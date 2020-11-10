/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.weightVariant;

import cecs429.index.DiskIndexWriter;

/**
 *
 * @author atandel
 */
public class WackyWeighting implements WeightingStrategy{

    DiskIndexWriter index = new DiskIndexWriter();
    @Override
    public double calculateWqt(int N, double dft) {
       double wqt = Math.max(0, Math.log((N-dft)/dft));
       return wqt;
    }

    @Override
    public double calculateWdt(int totalDoc, int docId, double tftd, String dir) {
       double avg_tftd=0;
       avg_tftd = index.getavgtftd(docId, dir);
       double numerator = 1 + Math.log(tftd);
       double denominator = 1 + Math.log(avg_tftd);
       double wdt = numerator/denominator;
       return wdt;
    }

    @Override
    public double calculateLd(int docId, String dir) {
        double bytesize = 0;
        bytesize = index.getbyteSize(docId, dir);
        double LD = Math.sqrt(bytesize);
       return LD;
    }
    
}
