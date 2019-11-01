package units;

import units.BaseUnit;
import units.BaseUnit.Dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;



/**
 * Unit formed by multiplying/dividing BaseUnits
 */
public class Unit {
	/*
     * instead of storing numerator units and denominator units, we can store
     * numerator and denominator units PER dimension. This allows us to group
     * units into dimensions. The reason this is helpful is as follows:
     * 
     * Suppose you are looking at units of momentum and have
     * kgm/s: KG * M / S
     * lbft/s: FT * LB / S
     * Simply looping through these to determine the conversion would require
     * dividing a kilogram by a foot and a meter by a pound. Basically grouping
     * would let us divide the correct units by each other to get the conversion.
     * aka invariant under switching order of inputed units.
     * 
     * Note: units are reduced in constructor so that there are none of the
     * same unit in both the numerator and denominator
     */
    /** BaseUnits in numerator grouped by dimension they measure */
    private final HashMap<Dimension, ArrayList<BaseUnit>> NUMERATOR;
    /** BaseUnits in denominator grouped by dimension they measure */
    private final HashMap<Dimension, ArrayList<BaseUnit>> DENOMINATOR;

    private final ArrayList<BaseUnit> NUMERATOR_LIST;
    private final ArrayList<BaseUnit> DENOMINATOR_LIST;

    /**
     * Total count of how many of each dimension are in the unit.
     * For example, kg m^2 -> Mass: 1, Length: 2
     */
    private final HashMap<Dimension, Integer> DIMENSIONS;
    /**
     * Coefficient in front of unit resulting from cancellation within
     * same dimension. For example, if unit is a kg inch/cm then it is just
     * 2.54 kg
     */
    private final double COEFF;



    /** Whether to store conversions to other units. Trade-off between computation time and memory space. */
    private boolean storeConversions = false;
    /** Stored conversions from a CompositeUnit of the same Dimension to this unit/other unit */
    private HashMap<Unit, Double> conversions = new HashMap<Unit, Double>();





    /**
     * Warning: this constructor may modify numerator and denominator variables
     * 
     * Create a CompositeUnit with a list of numerator and denominator units
     * 
     * @param numerator list of numerator units
     * @param denominator list of denominator units
     * @param coeff0
     */
    public Unit(List<BaseUnit> numerator, List<BaseUnit> denominator, double coeff0) {
        HashMap<Dimension, ArrayList<BaseUnit>> nums   = new HashMap<Dimension, ArrayList<BaseUnit>>();
        HashMap<Dimension, ArrayList<BaseUnit>> denoms = new HashMap<Dimension, ArrayList<BaseUnit>>();
        ArrayList<BaseUnit> numsList = new ArrayList<BaseUnit>();
        ArrayList<BaseUnit> denomsList = new ArrayList<BaseUnit>();
        HashMap<Dimension, Integer> dims = new HashMap<Dimension, Integer>();
        double coeff = coeff0;

        Dimension[] dimList = Dimension.values();

        for (int i = 0; i < dimList.length; i++) {
            Dimension dim = dimList[i];

            nums.put(dim, new ArrayList<BaseUnit>());
            denoms.put(dim, new ArrayList<BaseUnit>());
            dims.put(dim, 0);
        }



        for (int i = 0; i < numerator.size(); i++) {
            BaseUnit num = numerator.get(i);

            for (int j = 0; j < denominator.size(); j++) {
                BaseUnit denom = denominator.get(j);

                if (num.getDimension() == denom.getDimension()) {
                	// perDenom nums/denom = 1
                	// nums/denom = 1/perDenom
                    coeff /= num.per(denom);

                    numerator.remove(i);
                    denominator.remove(j);

                    i--;
                }
            }
        }

        for (int i = 0; i < numerator.size(); i++) {
            BaseUnit num = numerator.get(i);
            Dimension dim = num.getDimension();

            nums.get(dim).add(num);
            dims.put(dim, dims.get(dim) + 1);
            numsList.add(num);
        }

        for (int i = 0; i < denominator.size(); i++) {
            BaseUnit denom = denominator.get(i);
            Dimension dim = denom.getDimension();

            denoms.get(dim).add(denom);
            dims.put(dim, dims.get(dim) - 1);
            denomsList.add(denom);
        }

        NUMERATOR = nums;
        DENOMINATOR = denoms;
        DIMENSIONS = dims;
        COEFF = coeff;
        NUMERATOR_LIST = numsList;
        DENOMINATOR_LIST = denomsList;
    }

    /**
     * Warning: this constructor may modify numerator and denominator variables
     * 
     * Create a CompositeUnit with a list of numerator and denominator units
     * 
     * @param numerator list of numerator units
     * @param denominator list of denominator units
     */
    public Unit(List<BaseUnit> numerator, List<BaseUnit> denominator) {
        this(numerator, denominator, 1);
    }

    public Unit(BaseUnit[] numerator, BaseUnit[] denominator, double coeff0) {
        this(Arrays.asList(numerator), Arrays.asList(denominator), coeff0);
    }

    public Unit(BaseUnit[] numerator, BaseUnit[] denominator) {
        this(numerator, denominator, 1);
    }
    
    public Unit(Unit unit2, double coeff0) {
    	this(unit2.getNumeratorList(), unit2.getDenominatorList(), coeff0 * unit2.getCoefficient());
    }





    public double getCoefficient() {
        return COEFF;
    }

    public ArrayList<BaseUnit> getNumeratorTerms(Dimension dim) {
        return NUMERATOR.get(dim);
    }

    public ArrayList<BaseUnit> getNumeratorList() {
        return (ArrayList<BaseUnit>) NUMERATOR_LIST.clone(); // heck java
    }

    public ArrayList<BaseUnit> getDenominatorTerms(Dimension dim) {
        return DENOMINATOR.get(dim);
    }

    public ArrayList<BaseUnit> getDenominatorList() {
        return (ArrayList<BaseUnit>) DENOMINATOR_LIST.clone(); // heck java
    }

    public int getDimension(Dimension dim) {
        return DIMENSIONS.get(dim);
    }

    public boolean isCompatible(Unit unit2) {
        Dimension[] dimList = Dimension.values();

        for (int i = 0; i < dimList.length; i++) {
            Dimension dim = dimList[i];

            if (getDimension(dim) != unit2.getDimension(dim)) {
                return false;
            }
        }

        return true;
    }

    public boolean isCompatible(BaseUnit bu) {
        return isCompatible(bu.getUnit());
    }

    public double per(Unit unit2) {
        if (storeConversions) {
            Double conv = conversions.get(unit2);

            if (conv != null) {
                return conv;
            }
        }

        if (!isCompatible(unit2)) {
            return 0; // ig
        }

        // x = this unit/that unit
        // 1 unit = x COEFF nums/denoms = COEFF2 nums2/denoms2
        // x = COEFF2/COEFF * nums2/nums / (denoms2/denoms)
        // nums.per(nums2) nums = nums2
        // nums2/nums = nums.per(nums2)

        double per = unit2.getCoefficient() / COEFF;
        System.out.println(COEFF);
        System.out.println(unit2.getCoefficient());

        Dimension[] dimList = Dimension.values();

        for (int i = 0; i < dimList.length; i++) {
            Dimension dim = dimList[i];

            ArrayList<BaseUnit> numTerms = getNumeratorTerms(dim);
            ArrayList<BaseUnit> numTerms2 = unit2.getNumeratorTerms(dim);

            for (int j = 0; j < numTerms.size(); j++) {
                per *= numTerms.get(j).per(numTerms2.get(j));
            }

            ArrayList<BaseUnit> denomTerms = getDenominatorTerms(dim);
            ArrayList<BaseUnit> denomTerms2 = unit2.getDenominatorTerms(dim);

            for (int j = 0; j < denomTerms.size(); j++) {
                per /= denomTerms.get(j).per(denomTerms2.get(j));
            }
        }

        if (storeConversions) {
            conversions.put(unit2, per);
        }

        return per;
    }

    public double per(BaseUnit bu) {
        return per(bu.getUnit());
    }

    public Unit multiply(Unit unit2) {
        ArrayList<BaseUnit> numList = unit2.getNumeratorList(); // this is a copy
        ArrayList<BaseUnit> denomList = unit2.getDenominatorList(); // this is also a copy

        numList.addAll(NUMERATOR_LIST); // can add whatever without causing problems
        denomList.addAll(DENOMINATOR_LIST); // can add whatever without causing problems

        return new Unit(numList, denomList, COEFF * unit2.getCoefficient());
    }

    public Unit multiply(BaseUnit unit2) {
        return multiply(unit2.getUnit());
    }

    public Unit divide(Unit unit2) {
        ArrayList<BaseUnit> numList = unit2.getNumeratorList(); // this is a copy
        ArrayList<BaseUnit> denomList = unit2.getDenominatorList(); // this is also a copy

        numList.addAll(DENOMINATOR_LIST); // can add whatever without causing problems
        denomList.addAll(NUMERATOR_LIST); // can add whatever without causing problems

        return new Unit(numList, denomList, COEFF / unit2.getCoefficient());
    }

    public Unit divide(BaseUnit unit2) {
        return divide(unit2.getUnit());
    }
}