const axios = require('axios');

const token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJjb20udmFuc2FoLmppcmEudmFuc2FoLXBsdWdpbiIsImlhdCI6MTc2NjU2MTY1Niwic3ViIjoiNzEyMDIwOjVjZjJhNjg5LTI4ODgtNDNjMC1hMTI2LTUwMDM5MzgzNGJiMyIsImV4cCI6Mjc2NjU2MTY1NiwiYXVkIjpbImMzZDVkMzIzLWVmMTQtMzhiOS04MWI1LTNjMDg4Y2JhNjJmNiJdLCJ0eXBlIjoiY29ubmVjdCJ9.nH_s70cNAbYASUYz3W7xDRLtxlRWicAVf2uVXOUggps";

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
    console.log('Token length:', token.length);
    console.log('Token preview:', token.substring(0, 50) + '...\n');

    const response = await axios.post(
      'https://mattdev.vansahnode.app/api/v1/cucumber/import',
      testPayload,
      {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        validateStatus: () => true
      }
    );

    console.log('Status:', response.status);
    console.log('Response:', JSON.stringify(response.data, null, 2));

  } catch (error) {
    console.error('Request failed:', error.message);
    if (error.response) {
      console.error('Response:', error.response.data);
    }
  }
}

testAPI();

