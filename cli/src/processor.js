const axios = require('axios');
const FormData = require('form-data');

/**
 * Extract test case key from scenario tags
 */
function extractTestCaseKey(tags) {
  if (!tags || tags.length === 0) return null;

  for (const tag of tags) {
    const tagName = tag.name;

    // Pattern 1: @TC-{PROJECT}-C{NUMBER}
    if (tagName.startsWith('@TC-')) {
      return tagName.substring(4);
    }

    // Pattern 2: @TESTCASE-{PROJECT}-C{NUMBER}
    if (tagName.startsWith('@TESTCASE-')) {
      return tagName.substring(10);
    }

    // Pattern 3: Direct @{PROJECT}-C{NUMBER}
    const match = tagName.match(/^@([A-Z]+)-C(\d+)$/);
    if (match) {
      return tagName.substring(1);
    }
  }

  return null;
}

/**
 * Determine scenario result
 */
function determineScenarioResult(scenario) {
  if (!scenario.steps || scenario.steps.length === 0) {
    return 'PASSED';
  }

  for (const step of scenario.steps) {
    if (step.result.status === 'failed' || step.result.status === 'skipped') {
      return 'FAILED';
    }
  }
  
  return 'PASSED';
}

/**
 * Map status to Vansah result code
 */
function mapStatusToVansahCode(status) {
  // 0 = N/A, 1 = FAILED, 2 = PASSED, 3 = UNTESTED
  return status === 'PASSED' ? 2 : 1;
}

/**
 * Calculate total duration
 */
function calculateTotalDuration(steps) {
  if (!steps) return 0;
  return steps.reduce((total, step) => {
    return total + (step.result?.duration || 0);
  }, 0);
}

/**
 * Find first failed step index
 */
function findFailedStepIndex(steps) {
  if (!steps) return null;
  
  for (let i = 0; i < steps.length; i++) {
    if (steps[i].result.status === 'failed') {
      return i + 1;
    }
  }
  return null;
}

/**
 * Extract embeddings (attachments) from scenario and steps
 */
function extractEmbeddings(scenario) {
  const embeddings = [];
  
  // Check scenario-level embeddings
  if (scenario.embeddings && Array.isArray(scenario.embeddings)) {
    for (const embed of scenario.embeddings) {
      embeddings.push({
        data: embed.data,
        mimeType: embed.mime_type || 'application/octet-stream',
        name: embed.name || generateAttachmentName(embed.mime_type, embeddings.length),
        level: 'scenario'
      });
    }
  }
  
  // Check step-level embeddings
  if (scenario.steps) {
    scenario.steps.forEach((step, stepIndex) => {
      if (step.embeddings && Array.isArray(step.embeddings)) {
        for (const embed of step.embeddings) {
          embeddings.push({
            data: embed.data,
            mimeType: embed.mime_type || 'application/octet-stream',
            name: embed.name || generateAttachmentName(embed.mime_type, embeddings.length),
            level: 'step',
            stepIndex: stepIndex + 1,
            stepName: `${step.keyword}${step.name}`
          });
        }
      }
    });
  }
  
  return embeddings;
}

/**
 * Generate attachment name based on MIME type
 */
function generateAttachmentName(mimeType, index) {
  const extensions = {
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'text/plain': 'txt',
    'text/html': 'html',
    'application/json': 'json',
    'application/pdf': 'pdf'
  };
  
  const ext = extensions[mimeType] || 'bin';
  return `attachment_${index + 1}.${ext}`;
}

/**
 * Process single scenario
 */
function processScenario(scenario, featureName, options) {
  const testCaseKey = extractTestCaseKey(scenario.tags);

  if (!testCaseKey) {
    return {
      skipped: true,
      scenario: scenario.name,
      reason: 'No test case tag found'
    };
  }

  const result = determineScenarioResult(scenario);
  const resultCode = mapStatusToVansahCode(result);
  const failedStep = findFailedStepIndex(scenario.steps);

  // Build clean payload - only include non-null optional fields
  const testRun = {
    testCaseKey: testCaseKey,
    scenarioName: scenario.name,
    status: result,
    resultCode: resultCode
  };

  // Add optional fields only if they have values
  if (featureName) testRun.featureName = featureName;
  if (scenario.steps?.length > 0) {
    testRun.stepCount = scenario.steps.length;
    
    // Build actual result from steps
    const stepsSummary = scenario.steps.map((step, idx) => 
      `${idx + 1}. ${step.keyword}${step.name} - ${step.result.status.toUpperCase()}`
    ).join('\n');
    testRun.actualResult = `Cucumber Test Execution:\n${stepsSummary}`;
  }
  
  const duration = calculateTotalDuration(scenario.steps);
  if (duration > 0) testRun.duration = duration;
  
  if (failedStep) testRun.failedStep = failedStep;
  if (scenario.start_timestamp) testRun.timestamp = scenario.start_timestamp;
  if (options.stepLevel && scenario.steps) testRun.steps = scenario.steps;

  // Extract embeddings (screenshots, logs, etc.) if present
  const embeddings = extractEmbeddings(scenario);
  if (embeddings.length > 0) {
    testRun.embeddings = embeddings;
  }

  return testRun;
}

/**
 * Process entire Cucumber report
 */
function processCucumberReport(cucumberJson, options) {
  const results = {
    imported: 0,
    failed: 0,
    skipped: 0,
    testRuns: [],
    warnings: [],
    errors: []
  };

  for (const feature of cucumberJson) {
    if (!feature.elements) continue;

    for (const scenario of feature.elements) {
      try {
        const result = processScenario(scenario, feature.name, options);

        if (result.skipped) {
          results.skipped++;
          results.warnings.push({
            scenario: result.scenario,
            reason: result.reason
          });
        } else {
          results.testRuns.push(result);
          results.imported++;
        }
      } catch (error) {
        results.failed++;
        results.errors.push({
          scenario: scenario.name,
          error: error.message
        });
      }
    }
  }

  return results;
}

/**
 * Send results to Vansah API
 */
async function sendToVansah(results, options) {
  const payload = {
    projectKey: options.project,
    testFolderPath: options.folder,
    jiraIssueKey: options.issue,
    advancedTestPlanKey: options.atp,
    standardTestPlanKey: options.stp,
    sprintName: options.sprint,
    releaseName: options.release,
    environmentName: options.environment,
    stepLevelReporting: options.stepLevel || false,
    testRuns: results.testRuns
  };

  try {
    const response = await axios.post(
      `${options.apiUrl}/api/v1/cucumber/import`,
      payload,
      {
        headers: {
          'Authorization': options.token,  // SENZA "Bearer "
          'Content-Type': 'application/json'
        },
        timeout: 30000
      }
    );

    return {
      imported: response.data.imported || 0,
      failed: response.data.failed || 0,
      skipped: response.data.skipped || 0,
      testRuns: response.data.testRuns || [],
      warnings: response.data.warnings || [],
      errors: response.data.errors || []
    };

  } catch (error) {
    if (error.response) {
      const status = error.response.status;
      const message = error.response.data?.message || error.response.statusText;
      throw new Error(`API Error ${status}: ${message}`);
    } else if (error.request) {
      throw new Error('No response from Vansah API. Check your connection and API URL.');
    } else {
      throw new Error(`Request failed: ${error.message}`);
    }
  }
}

/**
 * Upload attachment to a test run
 */
async function uploadAttachment(runKey, embedding, options) {
  const form = new FormData();
  
  // Decode base64 to buffer
  const buffer = Buffer.from(embedding.data, 'base64');
  
  // Append file to form
  form.append('file', buffer, {
    filename: embedding.name,
    contentType: embedding.mimeType
  });

  try {
    const response = await axios.post(
      `${options.apiUrl}/api/v1/run/${runKey}/attachment`,
      form,
      {
        headers: {
          'Authorization': options.token,
          ...form.getHeaders()
        },
        timeout: 60000
      }
    );

    return {
      success: true,
      name: embedding.name,
      level: embedding.level,
      stepName: embedding.stepName
    };
  } catch (error) {
    const message = error.response?.data?.message || error.message;
    return {
      success: false,
      name: embedding.name,
      error: message
    };
  }
}

/**
 * Upload all attachments for test runs
 */
async function uploadAllAttachments(testRuns, options) {
  const results = {
    uploaded: 0,
    failed: 0,
    details: []
  };

  for (const testRun of testRuns) {
    if (!testRun.testRunId || !testRun.embeddings || testRun.embeddings.length === 0) {
      continue;
    }

    for (const embedding of testRun.embeddings) {
      const result = await uploadAttachment(testRun.testRunId, embedding, options);
      
      if (result.success) {
        results.uploaded++;
      } else {
        results.failed++;
      }
      
      results.details.push({
        testCaseKey: testRun.testCaseKey,
        testRunId: testRun.testRunId,
        ...result
      });
    }
  }

  return results;
}

module.exports = {
  extractTestCaseKey,
  determineScenarioResult,
  mapStatusToVansahCode,
  extractEmbeddings,
  processCucumberReport,
  sendToVansah,
  uploadAttachment,
  uploadAllAttachments
};

