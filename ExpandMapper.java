import java.util.*;
import java.util.zip.*;
import java.lang.Math.*;

public class ExpandMapper
{
	/*
	public static double[][] shrinkX(double src[][])
	{
	    int ydim          = src.length;
	    int xdim          = src[0].length; 
	    
	    int shrunken_xdim = xdim / 2;
	    if(xdim % 2 != 0)
	    	shrunken_xdim++;
	    double[][] dst    = new double[ydim][shrunken_xdim];
	    
	    for(int i = 0; i < ydim; i++)
	    {
	    	if(xdim % 2 == 0)
	    	{
	    	    for(int j = 0; j < xdim; j += 2)
	    	    {
	    		   dst[i][j / 2] = (src[i][j] + src[i][j + 1]) / 2;
	    	    }
	    	}
	    	else
	    	{
	    		int j = 0;
	    		for(j = 0; j < xdim - 1; j += 2)
	    		   dst[i][j / 2] = (src[i][j] + src[i][j + 1]) / 2;  
	    		dst[i][j / 2] = src[i][xdim - 1];
	    	}
	    }
	    return dst;
	}
	*/
	
	public static double[][] shrinkX(double [][] src)
	{
	    int ydim          = src.length;
	    int xdim          = src[0].length; 
	    
	    int shrunken_xdim = xdim / 2;
	    if(xdim % 2 != 0)
	    	shrunken_xdim++;
	   
	    double[][] dst          = new double[ydim][shrunken_xdim];
	   
	    for(int i = 0; i < ydim; i++)
	    {
	    	double a1 = src[i][0];
	    	if(xdim % 2 == 0)
	    	{ 
	    		int j = 0;
	    	    for(j = 0; j < xdim - 2; j += 2)
	    	    {
	    	    	double a2 = src[i][j + 1];
	    	        double a3 = src[i][j + 2];
	    	       
	    	        if(Math.abs(a1 - a2) < Math.abs((a1 + a3) - 2 * a2))
	    	        	dst[i][j / 2] = 2 * a2 - a1;
	    	        else
	    	        	dst[i][j / 2] = a3;
	    	        
	    	        if(dst[i][j / 2] < 0)
	    	        	dst[i][j / 2] = 0;
	    	        a1 = dst[i][j / 2];
	    	    }
	    	    double a2 = src[i][j];
    	        double a3 = src[i][j + 1];
    	        if(Math.abs(a1 - a2) < Math.abs((a1 + a3) - 2 * a2))
    	        	dst[i][j / 2] = 2 * a2 - a1;
    	        else
    	        	dst[i][j / 2] = a3;
    	        /*
    	        if(dst[i][j / 2] < 0)
    	        	dst[i][j / 2] = 0;
    	        */
    	        
	    	}
	    	else
	    	{
	    		int j = 0;
	    		for(j = 0; j < xdim - 2; j += 2)
	    		{
	    			double a2 = src[i][j + 1];
	    	        double a3 = src[i][j + 2];
	    	       
	    	        if(Math.abs(a1 - a2) < Math.abs((a1 + a3) - 2 * a2))
	    	        	dst[i][j / 2] = 2 * a2 - a1;
	    	        else
	    	        	dst[i][j / 2] = a3;
	    	        /*
	    	        if(dst[i][j / 2] < 0)
	    	        	dst[i][j / 2] = 0;
	    	        */
	    	        a1 = dst[i][j / 2];
	    		}
	    		
	    		dst[i][shrunken_xdim - 2] = src[i][xdim - 1];
	    		dst[i][shrunken_xdim - 1] = src[i][xdim - 1];
	    	}
	    }
	    return dst;
	}
	
	/*
	public static double[][] shrinkX(double [][] src, double [][] error)
	{
	    int ydim          = src.length;
	    int xdim          = src[0].length; 
	    

	    double min = 255;
	    double max = 0;
	    
	    for(int i = 0; i < ydim; i++)
	    {
	    	for(int j = 0; j < xdim; j++)
	    	{
	    	    if(src[i][j] < min)
	    	        min = src[i][j];
	    	    if(src[i][j] > max)
	    	        max = src[i][j];
	    	}
	    }
	    
	    int shrunken_xdim = xdim / 2;
	    if(xdim % 2 != 0)
	    	shrunken_xdim++;
	   
	    double[][] dst          = new double[ydim][shrunken_xdim];
	   
	    for(int i = 0; i < ydim; i++)
	    {
	    	double a1 = src[i][0];
	    	if(xdim % 2 == 0)
	    	{ 
	    		int j = 0;
	    	    for(j = 0; j < xdim - 2; j += 2)
	    	    {
	    	    	double a2 = src[i][j + 1];
	    	        double a3 = src[i][j + 2];
	    	       
	    	        if(Math.abs(a1 - a2) < Math.abs((a1 + a3) - 2 * a2))
	    	        	dst[i][j / 2] = 2 * a2 - a1;
	    	        else
	    	        	dst[i][j / 2] = a3;
	    	        
	    	        if(dst[i][j / 2] < 0)
	    	        	dst[i][j / 2] = 0;
	    	        a1 = dst[i][j / 2];
	    	    }
	    	    double a2 = src[i][j];
    	        double a3 = src[i][j + 1];
    	        if(Math.abs(a1 - a2) < Math.abs((a1 + a3) - 2 * a2))
    	        	dst[i][j / 2] = 2 * a2 - a1;
    	        else
    	        	dst[i][j / 2] = a3;
    	        if(dst[i][j / 2] < 0)
    	        	dst[i][j / 2] = 0;
    	        
	    	}
	    	else
	    	{
	    		for(int j = 0; j < xdim - 1; j += 2)
	    		{
	    			double a2 = src[i][j + 1];
	    	        double a3 = src[i][j + 2];
	    	       
	    	        if(Math.abs(a1 - a2) < Math.abs((a1 + a3) - 2 * a2))
	    	        	dst[i][j / 2] = 2 * a2 - a1;
	    	        else
	    	        	dst[i][j / 2] = a3;
	    	        
	    	        if(dst[i][j / 2] < 0)
	    	        	dst[i][j / 2] = 0;
	    	        a1 = dst[i][j / 2];
	    		}
	    	}
	    }
	    return dst;
	}
	*/
	
	
	
	public static double[][] shrinkY(double src[][])
	{
		int ydim          = src.length;
		int xdim          = src[0].length;
		
		double min = 255;
		double max = 0;
		for(int i = 0; i < ydim; i++)
		{
			for(int j = 0; j < xdim; j++)
			{
			    if(src[i][j] < min)
			        min = src[i][j];
			    if(src[i][j] > max)
			        max = src[i][j];
			}
		}
		
		int shrunken_ydim = ydim / 2;
		if(ydim % 2 != 0)
			shrunken_ydim++;
		
		double[][] dst    = new double[shrunken_ydim][xdim];
	     
	    for(int j = 0; j < xdim; j++)
	    {
	    	if(ydim % 2 == 0)
	    	{
	    	    for(int i = 0; i < ydim; i += 2)
	    	    {
	    		    dst[i / 2][j] = (src[i][j] + src[i + 1][j]) / 2;
	    		    if(dst[i / 2][j] < min)
		    			dst[i / 2][j] = min;
	    	    }
	    	}
	    	else
	    	{
	    		int i = 0;
	    		for(i = 0; i < ydim - 1; i += 2)
	    		{
	    		    dst[i / 2][j] = (src[i][j] + src[i + 1][j]) / 2;	
	    		    if(dst[i / 2][j] < min)
		    			dst[i / 2][j] = min;
	    		}
	    		dst[i / 2][j] = src[i][j];
	    	}
	    }
	    return dst;
	}
	
	
	
	
	
	
	
	
	
	/*
	public static double[][] avg4(double src[][])
	{
		double [][] intermediate = shrinkX(src);
		double [][] dst          = shrinkY(intermediate);
		
		return dst;
	}
	*/
	
	public static double[][] avg4(double src[][])
	{
		int ydim = src.length;
		int xdim = src[0].length;
		
		boolean x_even = true;
		if(xdim % 2 != 0)
			x_even = false;
		
		boolean y_even = true;
		if(ydim % 2 != 0)
			y_even = false;
		
		double [][] intermediate1 = shrinkX(src);
		double [][] intermediate2 = shrinkY(intermediate1);
		
	
		return intermediate2;
	}
	
	
	public static double[][] expandX(double src[][], boolean even)
	{
		int ydim = src.length;
		int xdim = src[0].length;
		
		double [][] dst;
		
		if(even)
		{
			dst = new double[ydim][xdim * 2];	
			for(int i = 0; i < ydim; i++)
			{
				int j;
				for(j = 0; j < xdim - 1; j++)
				{
					dst[i][j * 2]     = src[i][j];
					dst[i][j * 2 + 1] = (src[i][j] + src[i][j + 1])/2;
				}
				dst[i][j * 2]     = (src[i][j] + src[i][j - 1]) / 2;
				dst[i][j * 2 + 1] = src[i][j];
			}
			return dst;
		}
		else
		{
			dst = new double[ydim][xdim * 2 - 1];
			for(int i = 0; i < ydim; i++)
			{
				int j;
				for(j = 0; j < xdim - 1; j++)
				{  
				    dst[i][2 * j]     = src[i][j];  
				    dst[i][2 * j + 1] = (src[i][j] + src[i][j + 1]) / 2;
				}
				
				dst[i][2 * j - 1] = src[i][j];
			}
			return dst;
		}
	}
	public static double[][] expandY(double src[][], boolean even)
	{
		int ydim = src.length;
		int xdim = src[0].length;
		
		if(even)
		{
			double [][] dst = new double [2 * ydim][xdim];
			int i;
			for(i = 0; i < ydim - 1; i++)
			{
				for(int j = 0; j < xdim; j++)
				{  
				    dst[2 * i][j]     = src[i][j];  
				    dst[2 * i + 1][j] = (src[i][j] + src[i + 1][j]) / 2;
				}
			}
			for(int j = 0; j < xdim; j++)
			{  
			    dst[2 * i][j]     = src[i][j];  
			    dst[2 * i + 1][j] = src[i][j];
			}
			
			return dst;
		}
		else
		{
			double [][] dst = new double [2 * ydim - 1][xdim];
			
			int i;
			for(i = 0; i < ydim - 1; i++)
			{
				for(int j = 0; j < xdim; j++)
				{  
				    dst[2 * i][j]     = src[i][j];  
				    dst[2 * i + 1][j] = (src[i][j] + src[i + 1][j]) / 2;
				}
			}
			for(int j = 0; j < xdim; j++)
				dst[2 * i][j] = src[i][j];
			return dst;	
		}
	}
	
	
	public static double[][] expand(double src[][], boolean x_even, boolean y_even)
	{
		double [][] intermediate = expandX(src, x_even);
		double [][] dst          = expandY(intermediate, y_even);
		return dst;
	}
	
	
	public static int getAbsoluteSum(int src[])
	{
		int length = src.length;
		int sum  = 0;
		
		for(int i = 0; i < length; i++)
		{
			sum += Math.abs(src[i]);
		}
		return(sum);
	}
	
	public static double getAbsoluteTotal(double src[])
	{
		int length = src.length;
		double total = 0;
		
		for(int i = 0; i < length; i++)
		{
			total += Math.abs(src[i]);
		}
		return(total);
	}
	
	public static double[] getDifference(double src[], double src2[])
	{
		int length = src.length;
		double [] difference = new double[length];
		
		for(int i = 0; i < length; i++)
		{
			difference[i] = src[i] - src2[i];
		}
		return(difference);
	}
	
	public static int[] getDifference(int src1[], int src2[])
	{
		int length = src1.length;
		int [] difference = new int[length];
		
		// Could throw an exception here, but will
		// just return uninitialized array the same length
		// as src1.
		if(src2.length == length)
		{
		    for(int i = 0; i < length; i++)
		    {
			    difference[i] = src1[i] - src2[i];
		    }
		}
		return(difference);
	}
	
	public static int[] getSum(int src1[], int src2[])
	{
		int length = src1.length;
		int [] difference = new int[length];
		
		if(src2.length == length)  // else return uninitialzed array.
		{
		    for(int i = 0; i < length; i++)
		    {
			    difference[i] = src1[i] + src2[i];
		    }
		}
		return(difference);
	}
	
}