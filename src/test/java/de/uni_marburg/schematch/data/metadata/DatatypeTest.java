package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.TestUtils;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatatypeTest {

    @Test
    void determineDatatype() {
        TestUtils.TestData testData = TestUtils.getTestData();
        Scenario scenario = new Scenario(testData.getScenarios().get("test2").getPath());
        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");

        assertEquals(Datatype.INTEGER, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(0))));
        assertEquals(Datatype.STRING, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(1))));
        assertEquals(Datatype.STRING, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(2))));
        assertEquals(Datatype.FLOAT, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(3))));
        assertEquals(Datatype.BOOLEAN, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(4))));
        assertEquals(Datatype.BOOLEAN, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(5))));
        assertEquals(Datatype.DATE, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(6))));
        assertEquals(Datatype.GEO_LOCATION, Datatype.determineDatatype(Datatype.calculateScores(sourceTable.getColumn(7))));
    }

    @Test
    void castDatatype() {
        TestUtils.TestData testData = TestUtils.getTestData();
        Scenario scenario = new Scenario(testData.getScenarios().get("test2").getPath());
        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");

        List<Integer> castedCol0 = Datatype.castToInt(sourceTable.getColumn(0));
        for (Integer i : castedCol0) {
            assertNotNull(i);
        }
        List<Float> castedCol3 = Datatype.castToFloat(sourceTable.getColumn(3));
        for (Float f : castedCol3) {
            assertNotNull(f);
        }
        List<Boolean> castedCol4 = Datatype.castToBoolean(sourceTable.getColumn(4));
        for (Boolean b : castedCol4) {
            assertNotNull(b);
        }
        List<Boolean> castedCol5 = Datatype.castToBoolean(sourceTable.getColumn(5));
        for (Boolean b : castedCol5) {
            assertNotNull(b);
        }
        List<Date> castedCol6 = Datatype.castToDate(sourceTable.getColumn(6));
        for (Date d : castedCol6) {
            assertNotNull(d);
        }

        Table targetTable = scenario.getTargetDatabase().getTableByName("authors");

        List<Date> castedCol2Target = Datatype.castToDate(targetTable.getColumn(2));
        assertNull(castedCol2Target.get(0));
        assertNotNull(castedCol2Target.get(1));
        assertNotNull(castedCol2Target.get(2));
    }
}