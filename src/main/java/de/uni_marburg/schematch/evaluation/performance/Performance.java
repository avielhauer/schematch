package de.uni_marburg.schematch.evaluation.performance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Performance {
     private int TP;
     private int FP;
     private float simScoreTP;
     private float simScoreFP;

     public float calculatePrecision() {
          return (float) TP / (TP + FP);
     }
     public float calculateNonBinaryPrecision() {
          if (simScoreTP + simScoreFP == 0) {
               return 0;
          } else {
               return simScoreTP / (simScoreTP + simScoreFP);
          }
     }
}
