package de.uni_marburg.schematch.data.metadata;
import de.uni_marburg.schematch.data.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class ColumnMetadata {
    final Map<String, Float> numMetaMap = new HashMap<>();
    final Map<String, String> stringMetaMap = new HashMap<>();

    public float getNumericMetadata(String field){
        return numMetaMap.get(field);
    }

    public String getStringMetadata(String field){
        return stringMetaMap.get(field);
    }

    public Column.Datatype getDatatype(){
        switch(stringMetaMap.get("datatype")) {
            case "INTEGER":
                return Column.Datatype.INTEGER;
            case "FLOAT":
                return Column.Datatype.FLOAT;
            case "TEXT":
                return Column.Datatype.TEXT;
            case "DATE":
                return Column.Datatype.DATE;
            default:
                return Column.Datatype.STRING;
        }
    }
}
