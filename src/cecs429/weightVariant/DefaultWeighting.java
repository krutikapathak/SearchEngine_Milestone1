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
public class DefaultWeighting implements WeightingStrategy{

    DiskIndexWriter index = new DiskIndexWriter();
    @Override
    public double calculateWqt(int N, double dft) {
        double wqt = Math.log(1 + (N/dft));
        return wqt;
    }

    @Override
    public double calculateWdt(int totalDoc, int docId, double tftd,String dir) {
        double wdt = 1 + Math.log(tftd);
        return wdt;
    }

    @Override
    public double calculateLd(int docId, String dir) {
        double ld = index.getDocWeight(docId, dir);
        return ld;
    }
    
}