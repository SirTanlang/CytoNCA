package org.cytoscape.CytoNCA.internal.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.CytoNCA.internal.Protein;
import org.cytoscape.CytoNCA.internal.ProteinUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;

public class DC extends Algorithm {

	public DC(Long networkID,ProteinUtil pUtil){
		super(networkID, pUtil);
	}
	@Override
	public ArrayList<Protein> run(CyNetwork inputNetwork, ArrayList<Protein> vertex, boolean isweight) {
		// TODO Auto-generated method stub
		currentNetwork = inputNetwork;
		this.isweight = isweight;
		this.vertex = vertex;
		int i,j;
		double score;
		double x = 1;
		
		if (!isweight) {
			
			for (i = 0; i < vertex.size(); i++) {
				score=0;
				List<CyEdge> adjlist = inputNetwork.getAdjacentEdgeList(vertex
						.get(i).getN(), Type.ANY);
				for(j=0;j<adjlist.size();j++){
					score++;
				}
			  vertex.get(i).setDC(score);
			  if (taskMonitor != null) {
	                taskMonitor.setProgress((x) / vertex.size());
	                x++;
	            }
			}
		} else if (isweight) {
			
			for (i = 0; i < vertex.size(); i++) {
				score=0;
				List<CyEdge> adjlist = inputNetwork.getAdjacentEdgeList(vertex
						.get(i).getN(), Type.ANY);
                for(j=0;j<adjlist.size();j++){
                	score+=inputNetwork.getRow(adjlist.get(j)).get("weight", Double.class);
                	
                }
                System.out.println("@@@"+score);
                vertex.get(i).setDCW(score);
                if (taskMonitor != null) {
	                taskMonitor.setProgress((x) / vertex.size());
	                x++;
	            }
			}
		}
		return vertex;
	}

}
