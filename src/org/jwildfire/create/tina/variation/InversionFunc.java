package org.jwildfire.create.tina.variation;

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import java.math.BigInteger;
import static org.jwildfire.base.mathlib.MathLib.EPSILON;
import static org.jwildfire.base.mathlib.MathLib.M_2PI;
import static org.jwildfire.base.mathlib.MathLib.M_PI;
import static org.jwildfire.base.mathlib.MathLib.atan2;
import static org.jwildfire.base.mathlib.MathLib.sin;
import static org.jwildfire.base.mathlib.MathLib.cos;
import static org.jwildfire.base.mathlib.MathLib.fabs;
import static org.jwildfire.base.mathlib.MathLib.floor;
import static org.jwildfire.base.mathlib.MathLib.sqr;
import static org.jwildfire.base.mathlib.MathLib.pow;
import static org.jwildfire.base.mathlib.MathLib.sqrt;

import org.jwildfire.create.tina.base.Layer;
import org.jwildfire.create.tina.base.XForm;
import org.jwildfire.create.tina.base.XYZPoint;

/**
 * InversionFunc, a variation for inversion geometry transformations
 * Supports standard circle inversion
 * Includes x0, y0, z0 params for specifying origin translationa specific radius parameter
 *     (efffectively replaces pre- and post- transform coefficients 
 *      so don't need to keep them as mirrors of each other)
 *  also includes "draw_circles" param
 *      if 0 < draw_circles < 1, then that fraction of incoming points is used to 
 *      draw circle of inversion rather than doing the actual inversion
 *  In addition to standard circle inversion, can also be used for p-circle inversion as 
 *      described by Ramirez et al in "Generating Fractal Patterns by Using P-Circle Inversion" (2015)
 */
public class InversionFunc extends VariationFunc {
  // public class InversionFunc extends VariationFunc implements Guides {
  private static final long serialVersionUID = 1L;

  public static final String PARAM_XORIGIN = "xorigin";
  public static final String PARAM_YORIGIN = "yorigin";
  public static final String PARAM_ZORIGIN = "zorigin";
  public static final String PARAM_ROTATION = "rotation (pi * n radians)";
  public static final String PARAM_SCALE= "scale";
  public static final String PARAM_SHAPE = "shape";
  public static final String PARAM_A = "a";
  public static final String PARAM_B = "b";
  public static final String PARAM_C = "c";
  public static final String PARAM_D = "d";
  public static final String PARAM_E = "e";
  public static final String PARAM_F = "f";
  
  public static final String PARAM_INVERSION_MODE = "imode";
  public static final String PARAM_HIDE_UNINVERTED = "hide_uninverted";
  public static final String PARAM_RING_SCALE = "ring_scale";
  public static final String PARAM_RING_MODE = "ring_mode";
  public static final String PARAM_P= "p";
  public static final String PARAM_P2 = "p2";
  public static final String PARAM_DRAW_CIRCLE = "draw_circle";
  public static final String PARAM_SHAPE_THICKNESS = "shape_thickness";
  public static final String PARAM_GUIDES_ENABLED = "guides_enabled";
  public static final String PARAM_PASSTHROUGH = "passthrough";
  
  private static final String PARAM_DIRECT_COLOR_MEASURE = "color_measure";
  private static final String PARAM_DIRECT_COLOR_GRADIENT = "color_gradient";
  // private static final String PARAM_DIRECT_COLOR_THRESHOLDING = "color_thresholding";
  private static final String PARAM_COLOR_LOW_THRESH = "color_low_threshold";
  private static final String PARAM_COLOR_HIGH_THRESH = "color_high_threshold";
  
  private static final String[] paramNames = { 
    PARAM_SCALE, PARAM_ROTATION, 
    PARAM_SHAPE, 
    PARAM_INVERSION_MODE, PARAM_HIDE_UNINVERTED, 
    PARAM_RING_MODE, 
    PARAM_RING_SCALE,
    PARAM_P, PARAM_P2, PARAM_DRAW_CIRCLE, PARAM_SHAPE_THICKNESS, PARAM_PASSTHROUGH, PARAM_GUIDES_ENABLED, 
    PARAM_A, PARAM_B, PARAM_C, PARAM_D, PARAM_E, PARAM_F, 
    PARAM_DIRECT_COLOR_MEASURE, PARAM_DIRECT_COLOR_GRADIENT, 
    PARAM_COLOR_LOW_THRESH, PARAM_COLOR_HIGH_THRESH
  };
  
  public static int IGNORE_RING = 0;
  public static int INVERSION_INSIDE_RING_ONLY = 1;
  public static int INVERSION_OUTSIDE_RING_ONLY = 2;

  public static int STANDARD = 0;
  public static int EXTERNAL_INVERSION_ONLY = 1;
  public static int INTERNAL_INVERSION_ONLY = 2;
  public static int EXTERNAL_RING_INVERSION_ONLY = 3;
  public static int INTERNAL_RING_INVERSION_ONLY = 4;
  
  public static int CIRCLE = 0;
  public static int ELLIPSE = 1;
  public static int HYPERBOLA = 2;
  public static int REGULAR_POLYGON = 3;
  public static int RHODONEA = 4;
  public static int SUPERSHAPE = 5;
  
// direct color modes
public static int DST_DISTANCE_FROM_BOUNDARY = 1;
public static int RADIAL_DIFFERENCE1 = 10;
// additional possible color modes, not yet implemented
 // public static int SRC_DISTANCE_FROM_BOUNDARY = 0;
 // public static int SRC_DISTANCE_FROM_CENTER = 2;
 // public static int DST_DISTANCE_FROM_CENTER = 3;
 // public static int SIGNED_SRC_DISTANCE_FROM_BOUNDARY = 4;
 // public static int SIGNED_DST_DISTANCE_FROM_BOUNDARY = 5;
 // public static int SIGNED_SRC_DISTANCE_FROM_CENTER = 6;
 //  public static int SIGNED_DST_DISTANCE_FROM_CENTER = 7;
  
  private static final int OFF = 0;
  private static final int NONE = 0;
  private static final int COLORMAP_CLAMP = 1;
  private static final int COLORMAP_WRAP = 2;
  // color thresholding
  // private static final int PERCENT = 0;
  // private static final int VALUE = 1;
  
  ParametricShape shape;
  boolean draw_guides = false;
  double rotation_pi_fraction = 0;
  double shape_rotation_radians;
  double scalesqr;
  
  public void setDrawGuides(boolean draw) {
    draw_guides = draw;
  }
  
  public boolean getDrawGuides() { return draw_guides; }
  
  class PolarPoint2D {
    public double x;
    public double y;
    public double r;
    // public double t;
  }

  // abstract class ParametricShape {
  abstract class ParametricShape {
    public PolarPoint2D ptemp = new PolarPoint2D();
    public abstract void getCurvePoint(double t, PolarPoint2D point);
    public abstract double getPeriod();
    
    /** simple shapes will not have more than one intersection along a given ray out from shape center */
    public boolean simpleShape() { return true; }

    public PolarPoint2D getCurvePoint(double t) {
      PolarPoint2D outpoint = new PolarPoint2D();
      getCurvePoint(t, outpoint);
      return outpoint;
    }
    
    // find intersection nearest to point pIn of line from point pIn to "center" of shape
    //    (where "center" is defined by the shape object, 
    //       and is meant to usually be the centroid of the object)
    public void getMaxCurvePoint(double tin, PolarPoint2D pout) {
      // if doesn't intersect self, will only be one point of intersection
      if (simpleShape()) {
        getCurvePoint(tin, pout);
        return;
      }
      ptemp.r = 0;
      ptemp.x = 0;
      ptemp.y = 0;
      pout.r = -0.0001;
      pout.x = 0;
      pout.y = 0;

      // don't need to deal with shape rotation here, as it is handled in getCurvePoint() call
      int theta_count = (int)(getPeriod() / M_2PI);
      
      for (int i=0; i<theta_count; i++) {
        double theta = tin + (i * M_PI);
        getCurvePoint(theta, ptemp);
        if (abs(ptemp.r) > pout.r) {
          pout.x = ptemp.x;
          pout.y = ptemp.y;
          pout.r = abs(ptemp.r);
        }
      }
    }
  }
  
  class Circle extends ParametricShape {

    @Override
    public double getPeriod() { return M_2PI; }
    
    @Override
    public void getCurvePoint(double tin, PolarPoint2D point) {
      // for consistency with other shapes, though rotation shouldn't matter for the circle...
      double t = tin - shape_rotation_radians;  
      double r = scale;
      point.r = r;
      point.x = r * cos(tin);
      point.y = r * sin(tin);
    }
  }
  
  /**
   *  uses parameter a as number of sides for polygon
   * 
   */
  class RegularPolygon extends ParametricShape {
    
    @Override 
    public double getPeriod() { return M_2PI; }
    
    @Override
    // parametric polygon equation derived from: 
    //    http://math.stackexchange.com/questions/41940/is-there-an-equation-to-describe-regular-polygons
    //    http://www.geogebra.org/m/157867
    public void getCurvePoint(double tin, PolarPoint2D point) {
      double theta = abs((tin - shape_rotation_radians) % M_2PI);
      // double t = theta - shape_rotation_radians;
      double n = Math.floor(a);
      // double r = cos(M_PI/n) / cos(t%(M_2PI/n) - M_PI/n);
      double r = cos(M_PI/n) / cos(theta%(M_2PI/n) - M_PI/n);
      r *= scale;
      point.r = r;
      point.x = r * cos(tin);
      point.y = r * sin(tin);
    }
  }
  
  /**
   * uses param a, b as standard a, b params for an ellipse
   */
  class Ellipse extends ParametricShape {
    
    @Override 
    public double getPeriod() { return M_2PI; }
    
    @Override
    public void getCurvePoint(double tin, PolarPoint2D point) {
      // t is input angle modified by shape rotation
      double t = tin - shape_rotation_radians;
      double r = (a * b) / sqrt(sqr(b*cos(t)) + sqr(a*sin(t)));
      r *= scale;
      point.r = r;
      point.x = r * cos(tin);
      point.y = r * sin(tin);
    }
  }
  
  /*
   * uses param a, b as standard a, b params for a hyperbola
   */
  class Hyperbola extends ParametricShape {
    
    @Override 
    public double getPeriod() { return M_2PI; }
    
    @Override
    public void getCurvePoint(double tin, PolarPoint2D point) {
      double t = tin - shape_rotation_radians;
      double r2 = (a * a * b * b) / ((b * b * cos(t) * cos(t)) - (a * a * sin(t) * sin(t)));
      double r = sqrt(r2);
      r *= scale;
      point.r = r;
      point.x = r * cos(tin);
      point.y = r * sin(tin);
    }
  }
  
  /**
   * 
   */
  class SuperShape extends ParametricShape {
    @Override
    public double getPeriod() {
      return M_2PI;
    }

    @Override
    public void getCurvePoint(double tin, PolarPoint2D point) {
      double t = tin - shape_rotation_radians;

      // mapping params (a,b,c,d,e,f) to 
      //  naming convention of supershape (a,b,c,n1,n2,n3)
      // a = a param
      // b = b param
      double m = c;
      double n1 = d;
      double n2 = e;
      double n3 = f;
      
      double r = pow(
              (pow( fabs( (cos(m * t / 4))/a), n2) +
                      pow( fabs( (sin(m * t / 4))/b), n3)),
              (-1/n1));
      r *= scale;
      point.r = r;
      point.x = r * cos(tin);
      point.y = r * sin(tin);
    }
  }
  
  class Rhodonea extends ParametricShape {
    double period;
    public Rhodonea() {
      calcPeriod();
    }
    
    @Override
    public boolean simpleShape() { return false; }
    
    @Override 
    public double getPeriod() {
      return period;
    }
    @Override
    public void getCurvePoint(double tin, PolarPoint2D point) {
      double t = tin - shape_rotation_radians;
      double k = a/b;
      double r = cos(k * t) + c;
      r *= scale;
      point.r = r;
      point.x = r * cos(tin);
      point.y = r * sin(tin);
    }
    
    // sets Rhodonea.period
    public void calcPeriod() {
      double k = a/b;
      if ((k % 1) == 0) { // k is integer
        if ((k % 2) == 0) { // k is even integer, will have 2k petals and close in 2*Pi
          period = M_2PI; // (2PI)
        }
        else { // k is odd integer, will have k petals (or 2k if c!= 0)
          if (c != 0) {
            period = M_2PI;  // 2Pi
          } 
          else {
            period = M_PI;  // 1Pi
          } 
        }
      }
      else if ((a % 1 == 0) && (b % 1 == 0)) {
        double kn = a;
        double kd = b;
        // if kn and kd are integers,
        //   determine if kn and kd are relatively prime (their greatest common denominator is 1)
        //   using builtin gcd() function for BigIntegers in Java
        // and if they're not, make them
        BigInteger bigkn = BigInteger.valueOf((long) kn);
        BigInteger bigkd = BigInteger.valueOf((int) kd);
        int gcd = bigkn.gcd(bigkd).intValue();
        if (gcd != 1) {
          kn = kn / gcd;
          kd = kd / gcd;
        }
        
        // paraphrased from http://www.encyclopediaofmath.org/index.php/Roses_%28curves%29:
        //    If kn and kd are relatively prime, then the rose consists of 2*kn petals if either kn or kd are even, and kn petals if both kn and kd are odd
        //
        // paraphrased from http://mathworld.wolfram.com/Rose.html:
        //    If k=kn/kd is a rational number, then the curve closes at a polar angle of theta = PI * kd if (kn * kd) is odd, and 2 * PI * kd if (kn * kd) is even
        if ((kn % 2 == 0) || (kd % 2 == 0)) {
          period = kd * M_2PI; //
        }
        else {
          period = kd * M_PI;
        }
      }
      else {
        // a/b is irrational, just set to 2*pi for now
        period = M_2PI;
      }
    }
    
  }
  
  double scale = 1;
  double x0 = 0;
  double y0 = 0;
  double z0 = 0;
  double a = 1;
  double b = 1;
  double c = 0;
  double d = 0;
  double e = 0; 
  double f = 0;
  int shape_mode = CIRCLE;
  // if pass_through != 0, then allow (pass_through) fraction of points to pass through unaltered
  double passthrough = 0;
  
  double p = 2;
  double p2 = 2;
  double ring_scale = 1;
  int ring_mode = IGNORE_RING;
  double ring_rmin;
  double ring_rmax;
  double draw_shape = 0;
  double shape_thickness = 0;
  boolean guides_enabled = true;
  int inversion_mode = STANDARD;
  boolean hide_uninverted = false;
  
  // Color Handling
  private int direct_color_gradient = OFF;
  private int direct_color_measure = RADIAL_DIFFERENCE1;
  private double color_low_thresh = 0;
  private double color_high_thresh = 1.0;
  
  PolarPoint2D curve_point = new PolarPoint2D();
  PolarPoint2D circle_inversion_point = new PolarPoint2D();

  @Override
  public void transform(FlameTransformationContext pContext, XForm pXForm, XYZPoint pAffineTP, XYZPoint pVarTP, double pAmount) {
    double xin = pAffineTP.x;
    double yin = pAffineTP.y;
    double zin = pAffineTP.z;

    XYZPoint srcPoint, dstPoint;
    srcPoint = pAffineTP;
    dstPoint = pVarTP;

    double iscale;
    if (draw_guides && guides_enabled) { 
      double rnd = pContext.random();
      double theta = rnd * shape.getPeriod();
      double split = pContext.random() * 1.1;
      if (split < 1) {
        if (ring_mode == IGNORE_RING) {
          shape.getCurvePoint(theta, curve_point);
          pVarTP.x += curve_point.x;
          pVarTP.y += curve_point.y;
        }
        else {
          if (split < 0.5) {
            shape.getCurvePoint(theta, curve_point);
            pVarTP.x += curve_point.x;
            pVarTP.y += curve_point.y;
          }
          else if (split < 0.75) {
            // draw ring_rmin
            pVarTP.x += ring_rmin * cos(theta);
            pVarTP.y += ring_rmin * sin(theta);
          }
          else {
            pVarTP.x += ring_rmax * cos(theta);
            pVarTP.y += ring_rmax * sin(theta);
            // draw ring_rmax
          }
        }
      }
      else {  
        // no-op, leave 10% at shape origin
      }
      return;
    }
    
    if (draw_shape > 0) {
      double rnd = pContext.random();
      if (rnd < draw_shape) {
        double theta = pContext.random() * shape.getPeriod();
        shape.getCurvePoint(theta, curve_point);
        pVarTP.x += curve_point.x;
        pVarTP.y += curve_point.y;
        if (shape_thickness != 0) {
          pVarTP.x += 0.01 * (pContext.random() - 0.5) * shape_thickness;
          pVarTP.y += 0.01 * (pContext.random() - 0.5) * shape_thickness;
        }
        return;
      }
    }
    if (passthrough > 0) {
      double rnd = pContext.random(); 
      if (rnd < passthrough) {
        pVarTP.x += xin;
        pVarTP.y += yin;
        pVarTP.z += zin;
        return;
      }
    }
    // to do generalized inversion of input point P, 
    // need two other points:
    // O, the origin of inversion
    // S, the intersection of the line OP and the boundary of the shape
    // then output point P' = O + (d1(O,B)^2/d2(O,P)^2) * (P - O)
    // where d1 and d2 are distance metric functions 
    double tin = atan2(yin, xin);
    double rin = sqrt(xin*xin + yin*yin + zin*zin);
    boolean do_inversion;
    shape.getMaxCurvePoint(tin, curve_point);
    double rcurve = curve_point.r;
    double xcurve = curve_point.x;
    double ycurve = curve_point.y;
    if (inversion_mode == EXTERNAL_INVERSION_ONLY) {
      // only do inversion if input point is outside of circle
      do_inversion = rin > rcurve;
    }
    else if (inversion_mode == INTERNAL_INVERSION_ONLY) {
      // only do inversion if input point is inside of circle
      do_inversion = rin < rcurve;
    }
    else { // default to STANDARD mode ==> always do inversion
      do_inversion = true;
    }
    
    // if constraining to ring, then further restrict to:
    //   if inside of curve radius, make sure is > ring_min (based on ring scale)
    //   if outside of curve radius, make sure is < ring_max (based on inversion of ring_min)
    if (do_inversion && (ring_mode != IGNORE_RING) && (ring_scale != 1)) {
      if (ring_mode == INVERSION_INSIDE_RING_ONLY) {
        do_inversion = (rin >= ring_rmin) && (rin <= ring_rmax);
      }
      else if (ring_mode == INVERSION_OUTSIDE_RING_ONLY) {
        do_inversion = (rin <= ring_rmin) || (rin >= ring_rmax);
      }
    }

    if (do_inversion) {
      double num_scale = rcurve * rcurve;
      double denom_scale;
      if (p2 == 2) {
        num_scale = sqr(rcurve);
      }
      else {
        // num_scale = pow(rcurve, p2);
        num_scale = pow( (pow(abs(xcurve),p2) + pow(abs(ycurve),p2)), p2);
      }
      if (p == 2) {
        // iscale = (rcurve * rcurve) / (sqr(xin) + sqr(yin));
        denom_scale = sqr(xin) + sqr(yin);
      }
      else {
        // iscale = (rcurve * rcurve)/ pow( (pow(abs(xin),p) + pow(abs(yin),p)), 2.0/p);
        denom_scale = pow( (pow(abs(xin),p) + pow(abs(yin),p)), 2.0/p);
      }
      iscale = num_scale/denom_scale;
      
      double xout = iscale * xin;
      double yout = iscale * yin;
      double zout = iscale * zin;
      pVarTP.x += pAmount * xout;
      pVarTP.y += pAmount * yout;
      pVarTP.z += pAmount * zout;
      pVarTP.doHide = false;
      setColor(srcPoint, dstPoint, curve_point, pAmount);
    }
    else { // if didn't do inversion, check to see if should hide
      pVarTP.x += xin;
      pVarTP.y += yin;
      pVarTP.z += zin;
      pVarTP.doHide = hide_uninverted;
    }
  }
    
  public void setColor(XYZPoint srcPoint, XYZPoint dstPoint, PolarPoint2D curvePoint, double pAmount) {
    if (direct_color_measure != NONE && direct_color_gradient != OFF) {
      double val = 0;
      double[] sampled_vals;
      if (direct_color_measure == DST_DISTANCE_FROM_BOUNDARY) {
        double xdiff = dstPoint.x - curvePoint.x;
        double ydiff = dstPoint.y - curvePoint.y;
        double d = sqrt((xdiff * xdiff) + (ydiff * ydiff));
        val = d;
      }
      else if (direct_color_measure == RADIAL_DIFFERENCE1) {
        // compare dstPoint to standard circle inversion (ratio will be 1 if shape == CIRCLE && p == 2 && p2 == 2)
        // abs(dstPoint.radius/circleInversion.radius) ==>
        double r_shape_inversion = sqrt((dstPoint.x * dstPoint.x) + (dstPoint.y * dstPoint.y));
        
        double iscale = (scale * scale) / (sqr(srcPoint.x) + sqr(srcPoint.y));
        double cx = iscale * srcPoint.x * pAmount;
        double cy = iscale * srcPoint.y * pAmount;
        double r_circle_inversion = sqrt(cx * cx + cy * cy);
        double radial_ratio = r_shape_inversion / r_circle_inversion;
        val = radial_ratio;
      }
      else { return; }  // value not recognized, default back to normal coloring mode
        
      double baseColor = 0;
      double low_value, high_value;

      low_value = color_low_thresh;
      high_value = color_high_thresh;
      if (low_value > high_value) {
        double temp = low_value;
        low_value = high_value;
        high_value = temp;
      }
      
      if (val < low_value) { baseColor = 0; }
      else if (val >= high_value) { baseColor = 255; }
      else { baseColor = ((val - low_value)/(high_value - low_value)) * 255; }
      if (direct_color_gradient == COLORMAP_CLAMP) {
        dstPoint.rgbColor = false;
        dstPoint.color = baseColor / 255.0;
        if (dstPoint.color < 0) { dstPoint.color = 0; }
        if (dstPoint.color > 1.0) { dstPoint.color = 1.0; }
      }
      else if (direct_color_gradient == COLORMAP_WRAP) {
        dstPoint.rgbColor = false;
        // if val is outside range, wrap it around (cylce) to keep within range
        if (val < low_value) {
          val = high_value - ((low_value - val) % (high_value - low_value));
        }
        else if (val > high_value) {
          val = low_value + ((val - low_value) % (high_value - low_value));
        }
        baseColor = ((val - low_value)/(high_value - low_value)) * 255; 
        dstPoint.color = baseColor / 255.0;
        if (dstPoint.color < 0) { dstPoint.color = 0; }
        if (dstPoint.color > 1.0) { dstPoint.color = 1.0; }
      }
    } // END color_mode != normal

  }
  
  @Override
  public void init(FlameTransformationContext pContext, Layer pLayer, XForm pXForm, double pAmount) {
    shape_rotation_radians = M_PI * rotation_pi_fraction;
    scalesqr = scale * scale;
    
    // ring_max = ring_max_ratio * r;
    if (shape_mode == CIRCLE) {
      shape = new Circle();
    }
    else if (shape_mode == ELLIPSE) {
      shape = new Ellipse();
    }
    else if (shape_mode == HYPERBOLA) {
      shape = new Hyperbola();
    }
    else if (shape_mode == REGULAR_POLYGON) {
      shape = new RegularPolygon();
    }
    else if (shape_mode == RHODONEA) {
      shape = new Rhodonea();
    }
    else if (shape_mode == SUPERSHAPE) {
      shape = new SuperShape();
    }
    if (ring_scale <= scale) {
      ring_rmin = ring_scale * scale;
      ring_rmax =  (scale*scale) / ring_rmin;
    }
    else {
      ring_rmax = ring_scale * scale;
      ring_rmin = (scale*scale) / ring_rmax;
    }
  }

  @Override
  public String getName() {
    return "inversion";
  }

  @Override
  public String[] getParameterNames() {
    return paramNames;
  }

  @Override
  public Object[] getParameterValues() {
    return new Object[] { 
      scale, 
      rotation_pi_fraction, 
      shape_mode, 
      inversion_mode, hide_uninverted ? 1 : 0, 
      ring_mode, ring_scale, 
      p, p2, draw_shape, shape_thickness, passthrough, guides_enabled ? 1 : 0, 
      a, b, c, d, e, f, 
      direct_color_measure, direct_color_gradient, 
      color_low_thresh, color_high_thresh
    };
    
  }

  @Override
  public void setParameter(String pName, double pValue) {
    if (PARAM_SCALE.equalsIgnoreCase(pName)) {
      scale = pValue;
    }
    else if (PARAM_ROTATION.equalsIgnoreCase(pName)) {
      rotation_pi_fraction = pValue;
    }
    else if (PARAM_SHAPE.equalsIgnoreCase(pName)) {
      shape_mode = (int)pValue;
    }
    else if (PARAM_RING_MODE.equalsIgnoreCase(pName)) {
      ring_mode = (int)pValue;
    }
    else if (PARAM_RING_SCALE.equalsIgnoreCase(pName)) {
      ring_scale = pValue;
    }
    else if (PARAM_P.equalsIgnoreCase(pName)) {
      p = pValue;
    }
    else if (PARAM_P2.equalsIgnoreCase(pName)) {
      p2 = pValue;
    }
    else if (PARAM_INVERSION_MODE.equalsIgnoreCase(pName)) {
      inversion_mode = (int)pValue;
    }
    else if (PARAM_HIDE_UNINVERTED.equalsIgnoreCase(pName)) {
      hide_uninverted = (pValue == 1) ? true : false;
    }
    else if (PARAM_DRAW_CIRCLE.equalsIgnoreCase(pName)) {
      draw_shape = pValue;
    }
    else if (PARAM_SHAPE_THICKNESS.equalsIgnoreCase(pName)) {
      shape_thickness = pValue;
    }
    else if (PARAM_PASSTHROUGH.equalsIgnoreCase(pName)) {
      passthrough = pValue;
    }
    else if (PARAM_GUIDES_ENABLED.equalsIgnoreCase(pName)) {
      guides_enabled = (pValue == 1) ? true : false;
    }
    else if (PARAM_A.equalsIgnoreCase(pName)) {
      a = pValue;
    }
    else if (PARAM_B.equalsIgnoreCase(pName)) {
      b = pValue;
    }
    else if (PARAM_C.equalsIgnoreCase(pName)) {
      c = pValue;
    }
    else if (PARAM_D.equalsIgnoreCase(pName)) {
      d = pValue;
    }
    else if (PARAM_E.equalsIgnoreCase(pName)) {
      e = pValue;
    }
    else if (PARAM_F.equalsIgnoreCase(pName)) {
      f = pValue;
    }
    else if (PARAM_DIRECT_COLOR_MEASURE.equalsIgnoreCase(pName)) {
      direct_color_measure = (int)pValue;
    }
    else if (PARAM_DIRECT_COLOR_GRADIENT.equalsIgnoreCase(pName)) {
      direct_color_gradient = (int)pValue;
    }
    else if (PARAM_COLOR_LOW_THRESH.equalsIgnoreCase(pName)) {
      color_low_thresh = pValue;
    }
    else if (PARAM_COLOR_HIGH_THRESH.equalsIgnoreCase(pName)) {
      color_high_thresh = pValue;
    }
    else
      throw new IllegalArgumentException(pName);
  }

}
