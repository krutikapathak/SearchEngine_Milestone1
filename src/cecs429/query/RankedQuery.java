/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author atandel
 */
public class RankedQuery implements QueryComponent{
    
    private List<String> mTerms = new ArrayList<>();
    private Queue<Double> largest = new PriorityQueue<>(100);
    private int totalDoc = 0;
    
    public RankedQuery(List<String> terms){
        for (String term : terms) {
            mTerms.add(term);
	}
    }
    
    public RankedQuery(String terms, int totalDoc){
        List<String> termList = Arrays.asList(terms.split(" "));
	for (String term : termList) {
            mTerms.add(term);
	}
        this.totalDoc = totalDoc;
    }

    @Override
    public List<Posting> getPostings(Index index) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Posting> getPostings(Index index, String directory) {
        List<Posting> result = new ArrayList<Posting>();
        Map<Integer, Double> scoreMap = new HashMap<Integer, Double>();
        int k = 10;
        
        int N = totalDoc;
        double acc = 0.0;
        //for each term in query
        for (String term : mTerms) {
            List<Posting> termPosting = index.getPostingsDocandPos(term, directory);
            int dft = termPosting.size();
            double wqt = Math.log(1 + (N/dft));
            
            //for each document in posting
            for(Posting p : termPosting){
                int docId = p.getDocumentId();
                List<Integer> termsPosition = p.getmPositionId();
                int tftd = termsPosition.size();
                
                double wdt = 1 + Math.log(tftd);
                
                acc += (wdt*wqt); 
                
                if(scoreMap.containsKey(docId)){
                    double s = scoreMap.get(docId);
                    s += acc;
                    scoreMap.replace(docId, s);
                } else {
                    scoreMap.put(docId, acc);
                }                
            }
	}
        
        //loop over key(docId)
        for( int docId: scoreMap.keySet()){
            double accumulator = scoreMap.get(docId);
            double ld = getDocWeight(docId, directory);
            accumulator = accumulator/ld;
            scoreMap.replace(docId, accumulator);
        }
        
        
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>((Map.Entry<Integer, Double> x, Map.Entry<Integer, Double> y) -> Double.compare(y.getValue(), x.getValue()));
            

        //maintain a heap of size k.
        for (Map.Entry<Integer, Double> entry : scoreMap.entrySet()) {
            pq.offer(entry);
            if (pq.size() > k) {
                pq.poll();
            }
        }
        
        //get all elements from the heap
        while (pq.size() > 0) {
            Entry<Integer, Double> entry = pq.poll();
            Posting p = new Posting(entry.getKey(),entry.getValue());
            result.add(p);
        }
                
        return result;
    }
    
    
    public double getDocWeight(int docId, String directory){
            DataInputStream din;
            double docWeight = 0;
            try {
                    din = new DataInputStream(new FileInputStream(directory + "/index/docWeights.bin"));
                    int i = 0;
                    din.skipBytes(docId * 8);
                    docWeight = din.readDouble();
		} catch (Exception e) {
                    e.printStackTrace();
		}
            
            return docWeight;           
        }
    
}
