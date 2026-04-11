# parking-test

Run the parking module test suite and report the results clearly.

Execute the following command from the project root `D:/code/parking-management-system`:

```bash
mvn -pl ruoyi-admin -am \
  "-Dtest=ParkingSchemaScriptTest,ParkingMenuScriptTest,ParkingModuleSmokeTest,ParkingOverviewControllerTest" \
  "-Dsurefire.failIfNoSpecifiedTests=false" \
  test
```

After the build completes:

1. Report whether the build passed or failed
2. For every failing test, show the test class name, the assertion that failed, and the expected vs actual values
3. If all tests pass, confirm "All parking tests green" and list the test classes that ran
4. If additional test classes exist in `ruoyi-admin/src/test/java/com/ruoyi/parking/` beyond the four above, list them so the user knows they weren't included in this run

Do not suggest fixes — just report results. The user will decide what to do next.
