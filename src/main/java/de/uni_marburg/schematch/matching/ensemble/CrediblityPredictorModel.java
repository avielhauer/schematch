package de.uni_marburg.schematch.matching.ensemble;

import de.uni_marburg.schematch.data.Column;
import de.uni_marburg.schematch.data.Table;
import de.uni_marburg.schematch.matchtask.tablepair.TablePair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CrediblityPredictorModel implements Serializable {

    class ColomnPair{
        public ColomnPair(Column col1, Column col2) {
            this.col1 = col1;
            this.col2 = col2;
        }

        Column col1;
        Column col2;
    }
    List<ColomnPair> colomnPairs=new ArrayList<>();
     class ModelTrainedException extends Exception{
        public ModelTrainedException(){
            super("\"Model is Already Trained\"");
        }
    }
    boolean isTrained=false;
    List<TablePair> tablePairs=new ArrayList<>();
    public void addTablePair(TablePair tablePair) throws ModelTrainedException {
        if(!isTrained){
            tablePairs.add(tablePair);

        }
        else throw new ModelTrainedException();

    }

    List<Feature> features=new ArrayList<>();

    public void addFeature(Feature feature) throws ModelTrainedException {
        if(!isTrained){
            features.add(feature);

        }
        else throw new ModelTrainedException();

    }
    private void generateColumnPairs() throws ModelTrainedException {
        if(isTrained)
            throw new ModelTrainedException();
        else {

            for (TablePair tp:tablePairs)
            {
                Table source=tp.getSourceTable();
                Table target=tp.getTargetTable();
                for (int i = 0; i < source.getNumberOfColumns() ; i++) {
                    Column x=source.getColumn(i);
                    for (int j = 0; j < target.getNumberOfColumns() ; j++) {
                        Column y=target.getColumn(j);
                        colomnPairs.add(new ColomnPair(x,y));
                    }
                }
            }
        }
    }


}
