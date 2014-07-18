package org.cytoscape.CytoNCA.internal.algorithm.javaalgorithm;

import java.io.IOException;
import java.io.ObjectInputStream.GetField;

public abstract class Matrix {
	int width;
	int height;
	
	
	public int getHeight(){
		return height;
	}
	
	public int getWidth(){
		return width;
	}
	
	public abstract float getElement(int x, int y);
	public abstract boolean setElement(int x, int y, float value);
//	public abstract boolean makeSymTri(Matrix mtxQ, Matrix mtxT, float[] dblB, float[] dblC);
	
	/**
	 * 约化对称矩阵为对称三对角阵的豪斯荷尔德变换法
	 * 
	 * @param mtxQ - 返回豪斯荷尔德变换的乘积矩阵Q
	 * @param mtxT - 返回求得的对称三对角阵
	 * @param dblB - 一维数组，长度为矩阵的阶数，返回对称三对角阵的主对角线元素
	 * @param dblC - 一维数组，长度为矩阵的阶数，前n-1个元素返回对称三对角阵的
	 *               次对角线元素
	 * @return boolean型，求解是否成功
	 */
	public boolean makeSymTri(Matrix mtxQ, Matrix mtxT, float[] dblB, float[] dblC)
	{ 
		int i,j,k,u;
	    float h,f,g,h2, temp;
	    
		// 初始化矩阵Q和T
	   

		

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
	        
	        System.out.println(i +"  I  ");
	    }
	    
		for (i=0; i<=width-2; i++) 
			dblC[i]=dblC[i+1];
	    
		
	//	System.out.println(width +"****************");
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
	        System.out.println(i +"  I  ");
	    }

	    // 构造对称三对角矩阵
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
		    
		    System.out.println(i +"  I  ");
	    }

		return true;
	}
	
	/**
	 * 实对称三对角阵的全部特征值与特征向量的计算
	 * 
	 * @param dblB - 一维数组，长度为矩阵的阶数，传入对称三对角阵的主对角线元素；
	 *			     返回时存放全部特征值。
	 * @param dblC - 一维数组，长度为矩阵的阶数，前n-1个元素传入对称三对角阵的
	 *               次对角线元素
	 * @param mtxQ - 如果传入单位矩阵，则返回实对称三对角阵的特征值向量矩阵；
	 *			     如果传入MakeSymTri函数求得的矩阵A的豪斯荷尔德变换的乘积
	 *               矩阵Q，则返回矩阵A的特征值向量矩阵。其中第i列为与数组dblB
	 *               中第j个特征值对应的特征向量。
	 * @param nMaxIt - 迭代次数
	 * @param eps - 计算精度
	 * @return boolean型，求解是否成功
	 */

	public boolean computeEvSymTri(float[] dblB, float[] dblC, Matrix mtxQ, int nMaxIt, float eps)
	{
		int i,j,k,m,it,u,v;
	    float d,f,h,g,p,r,e,s, t;
	    
		// 初值
		int n = mtxQ.getWidth();
		dblC[n-1]=0.0f; 
		d=0.0f; 
		f=0.0f;
	    
		// 迭代计算

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
			
			System.out.println(j +"  J  ");
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
	        
	        System.out.println(i +"  I  ");
	    }
	    
		return true;
	}
	
}
