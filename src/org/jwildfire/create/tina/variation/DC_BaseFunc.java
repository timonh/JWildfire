package org.jwildfire.create.tina.variation;


import org.jwildfire.base.Tools;
import org.jwildfire.create.tina.base.Flame;
import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;
import org.jwildfire.create.tina.palette.RGBColor;
import org.jwildfire.create.tina.palette.RGBPalette;
import org.jwildfire.create.tina.render.FlameRenderer;

import js.glsl.G;
import js.glsl.vec2;
import js.glsl.vec3;


public  class DC_BaseFunc  extends VariationFunc {

	/*
	 * Base Classfor DC_Variations
	 * 
	 * Autor: Jesus Sosa
	 * Date: February 12, 2019
	 */

	private static final String PARAM_DC = "ColorOnly";
	private static final String PARAM_GRADIENT = "Gradient"; 

	private static final long serialVersionUID = 1L;

	private static final String[] paramNames = { PARAM_DC,PARAM_GRADIENT};

    
	
	int colorOnly=0;
	int gradient=0;

	
	@Override
	public void initOnce(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
		super.initOnce(pContext, pLayer, pXForm, pAmount);

	}	 

	@Override
	public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) 
	{

	}
	
	
	public RGBColor findKey(RGBPalette palette, int p_red,int	p_green,int p_blue){
	    int diff=1000000000; 
        RGBColor ret=new RGBColor();
        for (int i = 0; i < 256; i++)
        {
        	RGBColor color=palette.getColor(i);
        	int red=color.getRed();
        	int green=color.getGreen();
        	int blue=color.getBlue();

        	int dvalue= distance(p_red,p_green,p_blue,red,green,blue);
        	if (diff >dvalue) {
        		diff = dvalue;
        		ret = new RGBColor(red,green,blue);
        	}
        }
	    return ret;
	    }

	public int distance(int p_red,int p_green,int p_blue,int red,int green,int blue)
	{
    
    int dist_r = Math.abs(p_red - red);
    int dist_g = Math.abs(p_green - green);
    int dist_b = Math.abs(p_blue - blue);
    int dist_3d_sqd = (dist_r * dist_r) + (dist_g * dist_g) + (dist_b * dist_b);
    return dist_3d_sqd;
}
	
 	public vec3 getRGBColor(double xp,double yp)
 	{
       
        vec2 uv=new vec2(xp,yp);      	
        return new vec3(uv.x,uv.y,1.0);
 	}
 	
	@Override
	public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) 
	{

        vec3 color=new vec3(0.0); 
		 vec2 uV=new vec2(0.),p=new vec2(0.);
	       int[] tcolor=new int[3];  


		 
	     if(colorOnly==1)
		 {
			 uV.x=pAffineTP.x;
			 uV.y=pAffineTP.y;
		 }
		 else
		 {
	   			 uV.x=pContext.random()-0.5;
				 uV.y=pContext.random()-0.5;
		}
        
        color=getRGBColor(uV.x,uV.y);
        tcolor=dbl2int(color); 
        
        if(gradient==0)
        {
  	  	
    	  pVarTP.rgbColor  =true;;
    	  pVarTP.redColor  =tcolor[0];
    	  pVarTP.greenColor=tcolor[1];
    	  pVarTP.blueColor =tcolor[2];
    		
        }
        else
        {

            	Layer layer=pXForm.getOwner();
            	RGBPalette palette=layer.getPalette();      	  
          	    RGBColor col=findKey(palette,tcolor[0],tcolor[1],tcolor[2]);
          	    
          	  pVarTP.rgbColor  =true;;
          	  pVarTP.redColor  =col.getRed();
          	  pVarTP.greenColor=col.getGreen();
          	  pVarTP.blueColor =col.getBlue();

        }

        pVarTP.x+= pAmount*(uV.x);
        pVarTP.y+= pAmount*(uV.y);

	}
   
 	public int[] dbl2int(vec3 theColor)
  	{
  		int[] color=new int[3];

  		color[0] =  Math.max(0, Math.min(255, (int)Math.floor(theColor.x * 256.0D)));
  		color[1] =  Math.max(0, Math.min(255, (int)Math.floor(theColor.y * 256.0D)));
  		color[2] =  Math.max(0, Math.min(255, (int)Math.floor(theColor.z * 256.0D)));
  		return color;
  	}

	public String getName() {
		return "dc_";
	}

	public String[] getParameterNames() {
		return paramNames;
	}


	public Object[] getParameterValues() { //re_min,re_max,im_min,im_max,
		return new Object[] { colorOnly,gradient};
	}

	public void setParameter(String pName, double pValue) {

		if (pName.equalsIgnoreCase(PARAM_DC)) {
			colorOnly = (int)Tools.limitValue(pValue, 100 , 10000000);
		}
		else if (pName.equalsIgnoreCase(PARAM_GRADIENT)) {
			gradient = (int)Tools.limitValue(pValue, 0 , 1);
		}
		else
			throw new IllegalArgumentException(pName);
	}

	@Override
	public boolean dynamicParameterExpansion() {
		return true;
	}

	@Override
	public boolean dynamicParameterExpansion(String pName) {
		// preset_id doesn't really expand parameters, but it changes them; this will make them refresh
		return true;
	}	
	
}


