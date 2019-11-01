package units;

import units.BaseUnit;
import units.BaseUnit.Dimension;

public class Testing {
	// Length units
    
    public static void main(String[] args) {
        BaseUnit IN = new BaseUnit(Dimension.Length, 1);
        BaseUnit FT = new BaseUnit(IN, 1.0/12);
        BaseUnit CM = new BaseUnit(IN, 2.54);
        BaseUnit M = new BaseUnit(CM, 0.01);
        
    	System.out.println("feet per inch: " + FT.per(IN));
    	System.out.println("inches per foot: " + IN.per(FT));
    	System.out.println("inches per meter: " + IN.per(M));
    	System.out.println("cms per standard " + CM.getPerStandard());
    	
    	BaseUnit S   = new BaseUnit(Dimension.Time, 1);
    	
    	Unit IN_PER_S = (new UnitBuilder()).num(IN).denom(S).make();
    	Unit M_PER_S = (new UnitBuilder()).num(M).denom(S).make();
    	
    	System.out.println("in/s per m/s: " + IN_PER_S.per(M_PER_S));
    	
    	Unit M_PER_S2 = (new UnitBuilder()).num(M).denom(S, S).make();
    	Unit G = new Unit(M_PER_S2, 9.8);
    	
    	System.out.println("gs per m/s^2: " + G.per(M_PER_S2));
    	System.out.println("m/s^2 per g: " + M_PER_S2.per(G));
    	
    	BaseUnit LB = new BaseUnit(Dimension.Mass, 1);
    	BaseUnit KG = new BaseUnit(LB, 0.453592);
    	
    	Unit N = (new UnitBuilder()).num(KG, M).denom(S, S).make();
    	Unit FT_LB_PER_S2 = (new UnitBuilder()).num(LB, FT).denom(S, S).make();
    	
    	System.out.println("newtons per ft*lb/s^2: " + N.per(FT_LB_PER_S2));
    	
    	Unit COEFF_FRIC = (new UnitBuilder()).num(N).denom(N).make();
    	Unit COEFF_FRIC_2 = (new UnitBuilder()).num(FT_LB_PER_S2).denom(N).make();
    	
    	// 1 N/N = 1
    	// 0.1382548416 N = FT*LB/S^2
    	// 1 FT*LB/S^2/N = 7.2330197512591115
    	// x N/N = FT*LB/S^2/N -> x = 7.2330197512591115
    	
    	// x N = FT*LB/S^2 -> x = 
    	System.out.println("N/N per FT*LB/S^2/N: " + COEFF_FRIC.per(COEFF_FRIC_2));
    }
}
