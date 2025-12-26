const axios = require('axios');

const testPayload = {
  projectKey: "TD",
  testFolderPath: "dev test/",
  testRuns: [
    {
      testCaseKey: "TD-C1",
      scenarioName: "Successful test scenario",
      featureName: "Example Feature for Vansah Integration",
      status: "PASSED",
      resultCode: 2,
      stepCount: 3,
      duration: 616000
    }
  ]
};

async function testAPI() {
  try {
    console.log('Testing Vansah API endpoint...\n');
    console.log('Endpoint: https://mattdev.vansahnode.app/api/v1/cucumber/import\n');
    console.log('Payload:', JSON.stringify(testPayload, null, 2), '\n');

    const response = await axios.post(
      'https://mattdev.vansahnode.app/api/v1/cucumber/import',
      testPayload,
      {
        headers: {
          'Authorization': 'Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjb20udmFuc2FoLmppcmEudmFuc2FoLXBsdWdpbiIsImlhdCI6MTc2NjU2MTY1Niwic3ViIjoiNzEyMDIwOjVjZjJhNjg5LTI4ODgtNDNjMC1hMTI2LTUwMDM5MzgzNGJiMyIsImV4cCI6Mjc2NjU2MTY1NiwiYXVkIjpbImMzZDVkMzIzLWVmMTQtMzhiOS04MWI1LTNjMDg4Y2JhNjJmNiJdLCJ0eXBlIjoiY29ubmVjdCJ9.nH_s70cNAbYASUYz3W7xDRLtxlRWicAVf2uVXOUggps',
          'Content-Type': 'application/json'
        }
      }
    );

    console.log('✅ Success!');
    console.log('Response:', JSON.stringify(response.data, null, 2));

  } catch (error) {
    console.error('❌ Error:');
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
      console.error('Headers:', error.response.headers);
    } else {
      console.error(error.message);
    }
  }
}

testAPI();

