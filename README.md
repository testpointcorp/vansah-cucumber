# Vansah Cucumber Integration

Integrazione tra Cucumber Framework e Vansah Test Management per Jira. Questo progetto permette di inviare automaticamente i risultati dei test Cucumber a Vansah.

## Prerequisiti

- Java JDK 8 o superiore
- Maven 3.6 o superiore
- Vansah installato nel tuo workspace Jira
- Token Vansah Connect generato

## Configurazione

### 1. Configurare il Token Vansah

Modifica il file `.env` nella root del progetto e inserisci il tuo token Vansah:

```env
VANSAH_TOKEN=Your_Token_Here
VANSAH_API_URL=https://api.vansah.net
```

### 2. Configurazione Opzionale

Puoi configurare anche:

- `JIRA_ISSUE_KEY`: Chiave dell'issue Jira (per integrazione con JIRA Issue)
- `TEST_FOLDER_PATH`: Percorso della cartella test (per integrazione con Test Folder)
- `ADVANCED_TEST_PLAN_KEY`: Chiave dell'Advanced Test Plan
- `STANDARD_TEST_PLAN_KEY`: Chiave dello Standard Test Plan

## Struttura del Progetto

```
src/
├── main/java/com/testpoint/
│   └── vansah/
│       ├── VansahNode.java          # Classe principale per API Vansah
│       └── config/
│           └── VansahConfig.java    # Configurazione da .env
└── test/
    ├── java/com/testpoint/cucumber/
    │   ├── hooks/
    │   │   └── VansahHooks.java     # Hook Cucumber per integrazione
    │   ├── runners/
    │   │   └── CucumberTestRunner.java  # Runner per eseguire i test
    │   └── steps/
    │       └── ExampleSteps.java     # Esempio di step definitions
    └── resources/features/
        └── example.feature           # Esempio di feature file
```

## Utilizzo

### Eseguire i Test

```bash
mvn clean test
```

### Eseguire un Feature File Specifico

Modifica il `CucumberTestRunner.java` per specificare il feature file o usa i tag:

```bash
mvn test -Dcucumber.filter.tags="@TC-EXAMPLE-001"
```

## Come Funziona

1. **VansahHooks**: I hook `@Before` e `@After` vengono eseguiti automaticamente per ogni scenario:
   - `@Before`: Crea un test run in Vansah basato sul tag `@TC-` o `@TESTCASE-` nello scenario
   - `@After`: Registra il risultato finale dello scenario in Vansah

2. **Tag degli Scenario**: Usa i tag per identificare i test case:
   ```gherkin
   @TC-PROJ-123
   Scenario: My test scenario
   ```

3. **Logging dei Step**: Puoi loggare manualmente i risultati degli step nelle step definitions usando `VansahHooks.getVansahNode()`.

## Esempio di Feature File

```gherkin
@Vansah
Feature: Example Feature

  @TC-EXAMPLE-001
  Scenario: Successful test scenario
    Given I have a test scenario
    When I perform an action
    Then I verify the result
```

## Metodi Disponibili

La classe `VansahNode` fornisce i seguenti metodi principali:

- `addTestRunFromJIRAIssue(String testcase)`: Crea test run da JIRA Issue
- `addTestRunFromTestFolder(String testcase)`: Crea test run da Test Folder
- `addTestRunFromAdvancedTestPlan(String assetType, String testCaseKey)`: Crea test run da ATP
- `addTestRunFromStandardTestPlan(String testCaseKey)`: Crea test run da STP
- `addTestLog(String result, String comment, Integer testStepRow)`: Logga risultato step
- `addTestLog(String result, String comment, Integer testStepRow, File screenshot)`: Logga con screenshot

## Risoluzione Problemi

### Token non configurato
Assicurati che il file `.env` contenga `VANSAH_TOKEN` con un valore valido.

### Test run non creato
Verifica che lo scenario abbia un tag `@TC-` o `@TESTCASE-` con una chiave test case valida.

### Errori di connessione API
Controlla che `VANSAH_API_URL` sia corretto e che il token sia valido.

## Licenza

Questo progetto è fornito come esempio di integrazione tra Cucumber e Vansah.

