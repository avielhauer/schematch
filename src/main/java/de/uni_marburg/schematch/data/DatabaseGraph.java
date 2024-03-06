package de.uni_marburg.schematch.data;

import lombok.Getter;

import java.nio.file.Path;

public abstract class DatabaseGraph {
    public abstract Path exportPath();

    @Getter
    protected float graphBuildingTime = 0;
}
