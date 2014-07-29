package org.cytoscape.CytoNCA.internal.algorithm.javaalgorithm;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.work.TaskMonitor;

//import sun.misc.Cleaner;
//import sun.nio.ch.DirectBuffer;


public class LargeMatrix extends Matrix implements Closeable {
	private static final int MAPPING_SIZE = 1 << 30;
	private RandomAccessFile raf;
//	private final int width, height;
	private final List<MappedByteBuffer> mappings = new ArrayList();
	public static int filenum  = 1;
	private File f;
	
	public LargeMatrix(int width, int height) throws IOException{
		f = new File("LargeMatrix"+filenum);
		this.raf = new RandomAccessFile(f, "rw");
		filenum ++;
		try{
			this.width = width;
			this.height = height;
			long size = 4L * width * height;
			for(long offset = 0; offset < size; offset += MAPPING_SIZE){
				long size2 = size - offset > MAPPING_SIZE ? MAPPING_SIZE : size - offset;				
				mappings.add(raf.getChannel().map(FileChannel.MapMode.READ_WRITE, offset, size2));	
			}			
		}catch(IOException e){
			raf.close();
			throw e;
		}
	}
	
	public LargeMatrix(int width, int height, boolean flag){
		this.width = width;
		this.height = height;
	}
	
	protected long position(int i, int j){
		return (long)i * width + j;
	}
	
	public void closefile(){
		try {
			raf.getChannel().close();			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public File getFile(){
		return f;
	}
	
	public void deletefile(){
		f.delete();	
	}
	/*
	public int getHeight(){
		return height;
	}
	
	public int getWidth(){
		return width;
	}
	*/
	public float getElement(int i, int j){
	
		long p = position(i, j) * 4;
		int mapN = (int)p / MAPPING_SIZE;
		int offN = (int)p % MAPPING_SIZE;
		

		return  ((MappedByteBuffer) mappings.get(mapN)).getFloat(offN);
	}
	
	public boolean setElement(int i, int j, float value){
		if (j < 0 || j >= width || i < 0 || i >= height)
			return false;
		
		long p = position(i, j) * 4;
		int mapN = (int)p / MAPPING_SIZE;
		int offN = (int)p % MAPPING_SIZE;
		
	//	System.out.println(mapN +"  *  "+ offN);
		
		((MappedByteBuffer) mappings.get(mapN)).putFloat(offN, value);
		return true;
	}
	

public float[] getLine(int i){
		
		if (i < 0)
			return null;
		float[] line = new float[width];
		long s = position(i, 0) * 4;
		long t = position(i, width-1) * 4;
		
		int mapNs = (int)s / MAPPING_SIZE;
		int offNs = (int)s % MAPPING_SIZE;
		int mapNt = (int)t / MAPPING_SIZE;
		int offNt = (int)t % MAPPING_SIZE;
		
		if(mapNs == mapNt){
			for(int offset = offNs, count = 0; offset <= offNt; offset += 4, count ++){
				line[count] = ((MappedByteBuffer) mappings.get(mapNs)).getFloat(offset);
			}
		}else{
			int count = 0;
			for(int mapN = mapNs; mapN < mapNt; mapN++){
				for(int offset = offNs; offset < MAPPING_SIZE; offset += 4, count++){
					line[count] = ((MappedByteBuffer) mappings.get(mapN)).getFloat(offset);
				}
			}
			
			for(int offset = 0; offset < offNt; offset += 4, count++){
				line[count] = ((MappedByteBuffer) mappings.get(mapNt)).getFloat(offset);
			}
		}
			
		
		return line;
	}
	
	public void setLine(int i, float[] value){
		if (i < 0)
			return;
	
		long s = position(i, 0) * 4;
		long t = position(i, width-1) * 4;
		
		int mapNs = (int)s / MAPPING_SIZE;
		int offNs = (int)s % MAPPING_SIZE;
		int mapNt = (int)t / MAPPING_SIZE;
		int offNt = (int)t % MAPPING_SIZE;
		
		if(mapNs == mapNt){
			for(int offset = offNs, count = 0; offset <= offNt; offset += 4, count ++){
				 ((MappedByteBuffer) mappings.get(mapNs)).putFloat(offset, value[count]);
			}
		}else{
			int count = 0;
			for(int mapN = mapNs; mapN < mapNt; mapN++){
				for(int offset = offNs; offset < MAPPING_SIZE; offset += 4, count++){
				((MappedByteBuffer) mappings.get(mapN)).putFloat(offset, value[count]);
				}
			}
			
			for(int offset = 0; offset < offNt; offset += 4, count++){
				((MappedByteBuffer) mappings.get(mapNt)).putFloat(offset, value[count]);
			}
		}
			
	}
	
	
	public void initial(){
		long size = width * height * 4L;
		int mapN = (int)size / MAPPING_SIZE;
		int offN = (int)size % MAPPING_SIZE;
		
		for(int i = 0; i < mapN; i++){
			for(int offset = 0; offset < MAPPING_SIZE; offset += 4){
				((MappedByteBuffer) mappings.get(i)).putFloat(offset, 0.0f);
			}
		}
		
		for(int offset = 0; offset < offN; offset += 4){
			((MappedByteBuffer) mappings.get(mapN)).putFloat(offset, 0.0f);
		}
	
	}
	public void close() throws IOException {
		// TODO Auto-generated method stub	
		for(MappedByteBuffer mapping : mappings){
			clean(mapping);
		}
		raf.close();
		
	}
	
	private void clean(MappedByteBuffer mapping){
		if(mapping == null)
			return;
	//	Cleaner cleaner = ((DirectBuffer)mapping).cleaner();
	//	if(cleaner != null)
	//		cleaner.clean();
		mapping.clear();
		
	}

	/**
	 * Լ���Գƾ���Ϊ�Գ����Խ���ĺ�˹�ɶ��±任��
	 * 
	 * @param mtxQ - ���غ�˹�ɶ��±任�ĳ˻�����Q
	 * @param mtxT - ������õĶԳ����Խ���
	 * @param dblB - һά���飬����Ϊ����Ľ��������ضԳ����Խ�������Խ���Ԫ��
	 * @param dblC - һά���飬����Ϊ����Ľ�����ǰn-1��Ԫ�ط��ضԳ����Խ����
	 *               �ζԽ���Ԫ��
	 * @return boolean�ͣ�����Ƿ�ɹ�
	 */
	/*
	public boolean makeSymTri(Matrix mtxQ, Matrix mtxT, float[] dblB, float[] dblC)
	{ 
		int i,j,k,u;
	    float h,f,g,h2, temp;
	    
		// ��ʼ������Q��T
	    try {
			mtxQ = new LargeMatrix(width, width);
		    mtxT = new LargeMatrix(width, width);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		if (dblB == null || dblC == null)
			return false;

		for (i=0; i<=width-1; i++)
		{
			for (j=0; j<=width-1; j++)
			{ 
				mtxQ.setElement(i, j, getElement(i, j));
			}
		}

	    for (i=width-1; i>=1; i--)
	    { 
			h=0.0f;
	        if (i>1)
			{
				for (k=0; k<=i-1; k++)
	            { 
					temp = mtxQ.getElement(i, k);
					h += temp * temp;
				}
			}

	        if (h == 0.0f)
	        { 
				dblC[i]=0.0f;
	            if (i==1) 
					dblC[i]=mtxQ.getElement(i, i-1);
	            dblB[i]=0.0f;
	        }
	        else
	        { 
				dblC[i]=(float)Math.sqrt(h);
	            temp = mtxQ.getElement(i, i-1);
	            if (temp > 0.0f) 
					dblC[i]=-dblC[i];

	            h=h-temp*dblC[i];
	            mtxQ.setElement(i, i-1, temp-dblC[i]);
	            f=0.0f;
	            for (j=0; j<=i-1; j++)
	            { 
					mtxQ.setElement(j, i,mtxQ.getElement(i,j)/h);
	                g=0.0f;
	                for (k=0; k<=j; k++)
						g += mtxQ.getElement(j, k)*mtxQ.getElement(i, k);

					if (j+1<=i-1)
						for (k=j+1; k<=i-1; k++)
							g += mtxQ.getElement(k, j)*mtxQ.getElement(i, k);

	                dblC[j]=g/h;
	                f += g*mtxQ.getElement(j, i);
	            }
	            
				h2=f/(h+h);
	            for (j=0; j<=i-1; j++)
	            { 
					f=mtxQ.getElement(i, j);
	                g=dblC[j]-h2*f;
	                dblC[j]=g;
	                for (k=0; k<=j; k++)
	                { 
						u=j*width+k;
						temp = mtxQ.getElement(j, k);
	                    mtxQ.setElement(j, k, mtxQ.getElement(j, k) - f*dblC[k] - g*mtxQ.getElement(i, k));
	                }
	            }
	            
				dblB[i]=h;
	        }
	    }
	    
		for (i=0; i<=width-2; i++) 
			dblC[i]=dblC[i+1];
	    
		dblC[width-1]=0.0f;
	    dblB[0]=0.0f;
	    for (i=0; i<=width-1; i++)
	    { 
			if ((dblB[i]!=(float)0.0f) && (i-1>=0))
			{
				for (j=0; j<=i-1; j++)
	            { 
					g=0.0f;
					for (k=0; k<=i-1; k++)
						g += mtxQ.getElement(i, k) * mtxQ.getElement(k, j);

					for (k=0; k<=i-1; k++)
	                { 
						u=k*width+j;
						mtxQ.setElement(k, j, mtxQ.getElement(k, j) - g*mtxQ.getElement(k, i));
	                }
	            }
			}

	    
	        dblB[i]=mtxQ.getElement(i, i); 
	        mtxQ.setElement(i, i, 1.0f);
	        if (i-1>=0)
			{
				for (j=0; j<=i-1; j++)
	            { 
					mtxQ.setElement(i, j, 0.0f); 
					mtxQ.setElement(j, i, 0.0f);
				}
			}
	    }

	    // ����Գ����ԽǾ���
	    for (i=0; i<width; ++i)
		{
		    for (j=0; j<width; ++j)
			{
	            mtxT.setElement(i, j, 0);
	            k = i - j;
	            if (k == 0) 
		            mtxT.setElement(i, j, dblB[j]);
				else if (k == 1)
		            mtxT.setElement(i, j, dblC[j]);
				else if (k == -1)
		            mtxT.setElement(i, j, dblC[i]);
	        }
	    }

		return true;
	}
	*/
	
	/**
	 * ʵ�Գ����Խ����ȫ������ֵ�����������ļ���
	 * 
	 * @param dblB - һά���飬����Ϊ����Ľ���������Գ����Խ�������Խ���Ԫ�أ�
	 *			     ����ʱ���ȫ������ֵ��
	 * @param dblC - һά���飬����Ϊ����Ľ�����ǰn-1��Ԫ�ش���Գ����Խ����
	 *               �ζԽ���Ԫ��
	 * @param mtxQ - ������뵥λ�����򷵻�ʵ�Գ����Խ��������ֵ��������
	 *			     �������MakeSymTri������õľ���A�ĺ�˹�ɶ��±任�ĳ˻�
	 *               ����Q���򷵻ؾ���A������ֵ�����������е�i��Ϊ������dblB
	 *               �е�j������ֵ��Ӧ������������
	 * @param nMaxIt - ��������
	 * @param eps - ���㾫��
	 * @return boolean�ͣ�����Ƿ�ɹ�
	 */
/*
	public boolean computeEvSymTri(float[] dblB, float[] dblC, Matrix mtxQ, int nMaxIt, float eps)
	{
		int i,j,k,m,it,u,v;
	    float d,f,h,g,p,r,e,s, t;
	    
		// ��ֵ
		int n = mtxQ.getWidth();
		dblC[n-1]=0.0f; 
		d=0.0f; 
		f=0.0f;
	    
		// ��������

		for (j=0; j<=n-1; j++)
	    { 
			it=0;
	        h=eps*(Math.abs(dblB[j])+Math.abs(dblC[j]));
	        if (h>d) 
				d=h;
	        
			m=j;
	        while ((m<=n-1) && (Math.abs(dblC[m])>d)) 
				m=m+1;
	        
			if (m!=j)
	        { 
				do
	            { 
					if (it==nMaxIt)
						return false;

	                it=it+1;
	                g=dblB[j];
	                p=(dblB[j+1]-g)/(2.0f*dblC[j]);
	                r=(float)Math.sqrt(p*p+1.0f);
	                if (p>=0.0f) 
						dblB[j]=dblC[j]/(p+r);
	                else 
						dblB[j]=dblC[j]/(p-r);
	                
					h=g-dblB[j];
	                for (i=j+1; i<=n-1; i++)
						dblB[i]=dblB[i]-h;
	                
					f=f+h; 
					p=dblB[m]; 
					e=1.0f; 
					s=0.0f;
	                for (i=m-1; i>=j; i--)
	                { 
						g=e*dblC[i]; 
						h=e*p;
	                    if (Math.abs(p)>=Math.abs(dblC[i]))
	                    { 
							e=dblC[i]/p; 
							r=(float)Math.sqrt(e*e+1.0f);
	                        dblC[i+1]=s*p*r; 
							s=e/r; 
							e=1.0f/r;
	                    }
	                    else
						{ 
							e=p/dblC[i]; 
							r=(float)Math.sqrt(e*e+1.0f);
	                        dblC[i+1]=s*dblC[i]*r;
	                        s=1.0f/r; 
							e=e/r;
	                    }
	                    
						p=e*dblB[i]-s*g;
	                    dblB[i+1]=h+s*(e*g+s*dblB[i]);
	                    for (k=0; k<=n-1; k++)
	                    { 							
	                        h = mtxQ.getElement(k, i+1); 
	                        t = mtxQ.getElement(k, i);
							mtxQ.setElement(k, i+1, s*t + e*h);
	                        mtxQ.setElement(k, i, e*t - s*h);
	                    }
	                }
	                
					dblC[j]=s*p; 
					dblB[j]=e*p;
	            
				} while (Math.abs(dblC[j])>d);
	        }
	        
			dblB[j]=dblB[j]+f;
	    }
	    
		for (i=0; i<=n-1; i++)
	    { 
			k=i; 
			p=dblB[i];
	        if (i+1<=n-1)
	        { 
				j=i+1;
	            while ((j<=n-1) && (dblB[j]<=p))
	            { 
					k=j; 
					p=dblB[j]; 
					j=j+1;
				}
	        }

	        if (k!=i)
	        { 
				dblB[k]=dblB[i]; 
				dblB[i]=p;
	            for (j=0; j<=n-1; j++)
	            { 
	                p = mtxQ.getElement(j, i); 
					mtxQ.setElement(j, i, mtxQ.getElement(j, k)); 
					mtxQ.setElement(j, k, p);
	            }
	        }
	    }
	    
		return true;
	}
	
	*/
	
	
	/**
	 * Լ���Գƾ���Ϊ�Գ����Խ���ĺ�˹�ɶ��±任��
	 * 
	 * @param mtxQ - ���غ�˹�ɶ��±任�ĳ˻�����Q
	 * @param mtxT - ������õĶԳ����Խ���
	 * @param dblB - һά���飬����Ϊ����Ľ��������ضԳ����Խ�������Խ���Ԫ��
	 * @param dblC - һά���飬����Ϊ����Ľ�����ǰn-1��Ԫ�ط��ضԳ����Խ����
	 *               �ζԽ���Ԫ��
	 * @return boolean�ͣ�����Ƿ�ɹ�
	 */
	public boolean makeSymTri(float[] dblB, float[] dblC, TaskMonitor ts)
	{ 
		int i,j,k,u;
	    float h,f,g,h2, temp;
	    float[] line;
		// ��ʼ������Q��T
	   

		

		if (dblB == null || dblC == null)
			return false;

		/*
		for (i=0; i<=width-1; i++)
		{
			for (j=0; j<=width-1; j++)
			{ 
				mtxQ.setElement(i, j, getElement(i, j));
			}
		}
*/
		ts.setProgress(0);
		ts.setStatusMessage("Step 2...");
		
	    for (i=width-1; i>=1; i--)
	    { 
	    	line = getLine(i); 
	    	h=0.0f;
	        if (i>1)
			{
				for (k=0; k<=i-1; k++)
	            { 

				//	temp = mtxQ.getElement(i, k);
					temp = line[k];
					h += temp * temp;
				}
			}

	        if (h == 0.0f)
	        { 
				dblC[i]=0.0f;
	            if (i==1) 
				//	dblC[i]=mtxQ.getElement(i, i-1);
	            	dblC[i] = line[i-1];
	            dblB[i]=0.0f;
	        }
	        else
	        { 
				dblC[i]=(float)Math.sqrt(h);
	        //    temp = mtxQ.getElement(i, i-1);
	            temp = line[i-1];
	            if (temp > 0.0f) 
					dblC[i]=-dblC[i];

	            h=h-temp*dblC[i];
	         //   mtxQ.setElement(i, i-1, temp-dblC[i]);
	            line[i-1] = temp-dblC[i];
	            f=0.0f;
	            for (j=0; j<=i-1; j++)
	            { 
				//	mtxQ.setElement(j, i,mtxQ.getElement(i,j)/h);
					setElement(j, i, line[j]/h);
	                g=0.0f;
	                for (k=0; k<=j; k++)
					//	g += mtxQ.getElement(j, k)*mtxQ.getElement(i, k);
	                	g += getElement(j, k) * line[k];

					if (j+1<=i-1)
						for (k=j+1; k<=i-1; k++)
					//		g += mtxQ.getElement(k, j)*mtxQ.getElement(i, k);
							g += getElement(k, j) * line[k];
	                dblC[j]=g/h;
	                f += g*getElement(j, i);
	            }
	            
				h2=f/(h+h);
	            for (j=0; j<=i-1; j++)
	            { 
				//	f=mtxQ.getElement(i, j);
					f = line[j];
	                g=dblC[j]-h2*f;
	                dblC[j]=g;
	                for (k=0; k<=j; k++)
	                { 
						u=j*width+k;
						temp = getElement(j, k);
	            //        mtxQ.setElement(j, k, mtxQ.getElement(j, k) - f*dblC[k] - g*mtxQ.getElement(i, k));
	                    setElement(j, k, getElement(j, k) - f*dblC[k] - g*line[k]);
	                }
	            }
	            
				dblB[i]=h;
	        }
	        
	        setLine(i, line);
	        
	       ts.setProgress((float)(width - i)/width);
	    }
	    
		for (i=0; i<=width-2; i++) 
			dblC[i]=dblC[i+1];
	    
		
	//	System.out.println(width +"****************");
		dblC[width-1]=0.0f;
	    dblB[0]=0.0f;
	    ts.setProgress(0);
	    ts.setStatusMessage("Step 3...");
	    
	    for (i=0; i<=width-1; i++)
	    { 
			if ((dblB[i]!=(float)0.0f) && (i-1>=0))
			{
				for (j=0; j<=i-1; j++)
	            { 
					g=0.0f;
					for (k=0; k<=i-1; k++)
						g += getElement(i, k) * getElement(k, j);

					for (k=0; k<=i-1; k++)
	                { 
						u=k*width+j;
						setElement(k, j, getElement(k, j) - g*getElement(k, i));
	                }
	            }
			}

	    
	        dblB[i]=getElement(i, i); 
	        setElement(i, i, 1.0f);
	        if (i-1>=0)
			{
				for (j=0; j<=i-1; j++)
	            { 
					setElement(i, j, 0.0f); 
					setElement(j, i, 0.0f);
				}
			}
	        
	        ts.setProgress((float)i/width);
	    }

	    // ����Գ����ԽǾ���
	 /*
	    for (i=0; i<width; ++i)
		{   
			mtxT.setElement(i, i, dblB[i]);			
			   if(i > 0)
			mtxT.setElement(i, i-1, dblC[i-1]);
			   if(i < width-1)	
			mtxT.setElement(i, i+1, dblC[i+1]);
		        
		  
	    }
*/
		return true;
	}
	
	/**
	 * ʵ�Գ����Խ����ȫ������ֵ�����������ļ���
	 * 
	 * @param dblB - һά���飬����Ϊ����Ľ���������Գ����Խ�������Խ���Ԫ�أ�
	 *			     ����ʱ���ȫ������ֵ��
	 * @param dblC - һά���飬����Ϊ����Ľ�����ǰn-1��Ԫ�ش���Գ����Խ����
	 *               �ζԽ���Ԫ��
	 * @param mtxQ - ������뵥λ�����򷵻�ʵ�Գ����Խ��������ֵ��������
	 *			     �������MakeSymTri������õľ���A�ĺ�˹�ɶ��±任�ĳ˻�
	 *               ����Q���򷵻ؾ���A������ֵ�����������е�i��Ϊ������dblB
	 *               �е�j������ֵ��Ӧ������������
	 * @param nMaxIt - ��������
	 * @param eps - ���㾫��
	 * @return boolean�ͣ�����Ƿ�ɹ�
	 */

	public boolean computeEvSymTri(float[] dblB, float[] dblC, int nMaxIt, float eps, TaskMonitor ts)
	{
		int i,j,k,m,it,u,v;
	    float d,f,h,g,p,r,e,s, t;
	    
		// ��ֵ
		int n = getWidth();
		dblC[n-1]=0.0f; 
		d=0.0f; 
		f=0.0f;
	    
		ts.setProgress(0);
		ts.setStatusMessage("Step 4...");
		
		// ��������

		for (j=0; j<=n-1; j++)
	    { 
			it=0;
	        h=eps*(Math.abs(dblB[j])+Math.abs(dblC[j]));
	        if (h>d) 
				d=h;
	        
			m=j;
	        while ((m<=n-1) && (Math.abs(dblC[m])>d)) 
				m=m+1;
	        
			if (m!=j)
	        { 
				do
	            { 
					if (it==nMaxIt)
						return false;

	                it=it+1;
	                g=dblB[j];
	                p=(dblB[j+1]-g)/(2.0f*dblC[j]);
	                r=(float)Math.sqrt(p*p+1.0f);
	                if (p>=0.0f) 
						dblB[j]=dblC[j]/(p+r);
	                else 
						dblB[j]=dblC[j]/(p-r);
	                
					h=g-dblB[j];
	                for (i=j+1; i<=n-1; i++)
						dblB[i]=dblB[i]-h;
	                
					f=f+h; 
					p=dblB[m]; 
					e=1.0f; 
					s=0.0f;
	                for (i=m-1; i>=j; i--)
	                { 
						g=e*dblC[i]; 
						h=e*p;
	                    if (Math.abs(p)>=Math.abs(dblC[i]))
	                    { 
							e=dblC[i]/p; 
							r=(float)Math.sqrt(e*e+1.0f);
	                        dblC[i+1]=s*p*r; 
							s=e/r; 
							e=1.0f/r;
	                    }
	                    else
						{ 
							e=p/dblC[i]; 
							r=(float)Math.sqrt(e*e+1.0f);
	                        dblC[i+1]=s*dblC[i]*r;
	                        s=1.0f/r; 
							e=e/r;
	                    }
	                    
						p=e*dblB[i]-s*g;
	                    dblB[i+1]=h+s*(e*g+s*dblB[i]);
	                    for (k=0; k<=n-1; k++)
	                    { 							
	                        h = getElement(k, i+1); 
	                        t =getElement(k, i);
							setElement(k, i+1, s*t + e*h);
	                        setElement(k, i, e*t - s*h);
	                    }
	                }
	                
					dblC[j]=s*p; 
					dblB[j]=e*p;
	            
				} while (Math.abs(dblC[j])>d);
	        }
	        
			dblB[j]=dblB[j]+f;
			
			ts.setProgress((float)j/n);
	    }
	    
		ts.setProgress(0);
		ts.setStatusMessage("Step 5...");
		
		for (i=0; i<=n-1; i++)
	    { 
			k=i; 
			p=dblB[i];
	        if (i+1<=n-1)
	        { 
				j=i+1;
	            while ((j<=n-1) && (dblB[j]<=p))
	            { 
					k=j; 
					p=dblB[j]; 
					j=j+1;
				}
	        }

	        if (k!=i)
	        { 
				dblB[k]=dblB[i]; 
				dblB[i]=p;
	            for (j=0; j<=n-1; j++)
	            { 
	                p = getElement(j, i); 
					setElement(j, i, getElement(j, k)); 
					setElement(j, k, p);
	            }
	        }
	        
	        ts.setProgress((float)i/n);
	    }
	    
		return true;
	}
	
}
