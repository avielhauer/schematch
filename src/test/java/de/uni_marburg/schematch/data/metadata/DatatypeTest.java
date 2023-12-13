package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.TestUtils;
import de.uni_marburg.schematch.data.Scenario;
import de.uni_marburg.schematch.data.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatatypeTest {

    @Test
    void determineDatatype() {
        TestUtils.TestData testData = TestUtils.getTestData();
        Scenario scenario = new Scenario(testData.getScenarios().get("test2").getPath());
        Table sourceTable = scenario.getSourceDatabase().getTableByName("authors");

        assertEquals(Datatype.INTEGER, Datatype.determineDatatype(sourceTable.getColumn(0)));
        assertEquals(Datatype.STRING, Datatype.determineDatatype(sourceTable.getColumn(1)));
        assertEquals(Datatype.STRING, Datatype.determineDatatype(sourceTable.getColumn(2)));
        assertEquals(Datatype.FLOAT, Datatype.determineDatatype(sourceTable.getColumn(3)));
        assertEquals(Datatype.BOOLEAN, Datatype.determineDatatype(sourceTable.getColumn(4)));
        assertEquals(Datatype.BOOLEAN, Datatype.determineDatatype(sourceTable.getColumn(5)));
        //assertEquals(Datatype.DATE, Datatype.determineDatatype(sourceTable.getColumn(6)));
        //assertEquals(Datatype.GEO_LOCATION, Datatype.determineDatatype(sourceTable.getColumn(7)));
    }
}