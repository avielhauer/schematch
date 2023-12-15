package de.uni_marburg.schematch.data.metadata;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.metadata.dependency.InclusionDependency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioMetadata {
    Collection<InclusionDependency> sourceToTargetMetadata;
    Collection<InclusionDependency> targetToSourceMetadata;
    final Map<Column, Collection<InclusionDependency>> sourceToTargetMap = new HashMap<>();
    final Map<Column, Collection<InclusionDependency>> targetToSourceMap = new HashMap<>();

    public Collection<InclusionDependency> getSourceToTargetMetadata(Column column){
        return sourceToTargetMap.get(column);
    }

    public Collection<InclusionDependency> getTargetToSourceMetadata(Column column){
        return targetToSourceMap.get(column);
    }

    public Collection<InclusionDependency> getSourceToTargetMetadata(Column column, int size){
        return sourceToTargetMap.get(column).stream().filter(e -> e.getDependant().size() <= size).toList();
    }

    public Collection<InclusionDependency> getTargetToSourceMetadata(Column column, int size){
        return targetToSourceMap.get(column).stream().filter(e -> e.getDependant().size() <= size).toList();
    }

    public boolean contains(Column column){
        return sourceToTargetMap.containsKey(column) || targetToSourceMap.containsKey(column);
    }
}
