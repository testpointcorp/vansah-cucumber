#!/usr/bin/env node

const fs = require('fs');
const { program } = require('commander');
const { processCucumberReport, sendToVansah } = require('./processor');

program
  .name('vansah-cucumber-import')
  .description('Import Cucumber test results to Vansah')
  .version('1.0.0')
  .requiredOption('-r, --report <path>', 'Path to Cucumber JSON report')
  .requiredOption('-t, --token <token>', 'Vansah API token')
  .requiredOption('-p, --project <key>', 'Jira project key')
  .option('-f, --folder <path>', 'Test folder path in Vansah')
  .option('-i, --issue <key>', 'Jira issue key')
  .option('-a, --atp <key>', 'Advanced Test Plan key')
  .option('-s, --stp <key>', 'Standard Test Plan key')
  .option('--sprint <name>', 'Sprint name')
  .option('--release <name>', 'Release name')
  .option('--environment <name>', 'Environment name')
  .option('--step-level', 'Enable step-level reporting', false)
  .option('--api-url <url>', 'Vansah API URL', 'https://prodau.vansah.com')
  .option('-v, --verbose', 'Verbose output', false)
  .parse(process.argv);

const options = program.opts();

async function main() {
  try {
    // Validate required options
    if (!options.folder && !options.issue && !options.atp && !options.stp) {
      throw new Error('At least one context required: --folder, --issue, --atp, or --stp');
    }

    // 1. Read Cucumber report
    console.log(`📄 Reading report: ${options.report}`);
    
    if (!fs.existsSync(options.report)) {
      throw new Error(`Report file not found: ${options.report}`);
    }
    
    const reportContent = fs.readFileSync(options.report, 'utf8');
    const cucumberReport = JSON.parse(reportContent);
    console.log(`✓ Loaded ${cucumberReport.length} feature(s)`);

    // 2. Process report
    console.log('\n🔍 Processing scenarios...');
    const results = processCucumberReport(cucumberReport, options);
    console.log(`✓ Processed ${results.imported + results.skipped} scenario(s)`);

    if (results.imported === 0) {
      console.log('\n⚠️  No test cases to import (all skipped)');
      displayResults(results);
      process.exit(0);
    }

    // 3. Send to Vansah
    console.log('\n📤 Uploading to Vansah API...');
    const response = await sendToVansah(results, options);

    // 4. Display results
    displayResults(response);

    // 5. Exit
    const hasErrors = (response.failed > 0) || (response.errors && response.errors.length > 0);
    process.exit(hasErrors ? 1 : 0);

  } catch (error) {
    console.error('\n❌ Error:', error.message);
    if (options.verbose) {
      console.error(error.stack);
    }
    process.exit(1);
  }
}

function displayResults(results) {
  console.log('\n' + '═'.repeat(60));
  console.log('  VANSAH CUCUMBER IMPORT RESULTS');
  console.log('═'.repeat(60));

  console.log('\n📊 Summary:');
  console.log(`  ✅ Imported: ${results.imported}`);
  console.log(`  ❌ Failed:   ${results.failed}`);
  console.log(`  ⏭️  Skipped:  ${results.skipped}`);

  if (results.testRuns && results.testRuns.length > 0) {
    console.log('\n📋 Test Runs Created:');
    console.log('  ' + '─'.repeat(58));
    
    results.testRuns.forEach(tr => {
      const statusEmoji = tr.status === 'PASSED' ? '✅' : '❌';
      console.log(`  ${statusEmoji} ${tr.testCaseKey} → ${tr.testRunId || 'N/A'} (${tr.status})`);
    });
  }

  if (results.warnings && results.warnings.length > 0) {
    console.log('\n⚠️  Warnings:');
    results.warnings.forEach(w => {
      console.log(`  - "${w.scenario}": ${w.reason}`);
    });
  }

  if (results.errors && results.errors.length > 0) {
    console.log('\n❌ Errors:');
    results.errors.forEach(e => {
      console.log(`  - ${e.scenario}: ${e.error}`);
    });
  }

  console.log('\n' + '═'.repeat(60) + '\n');
}

main();

