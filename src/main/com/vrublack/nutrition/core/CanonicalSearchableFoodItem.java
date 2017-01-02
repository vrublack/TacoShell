package com.vrublack.nutrition.core;


public abstract class CanonicalSearchableFoodItem extends SearchableFoodItem
{
    private final static long serialVersionUID = 32535235L;

    /**
     * @return Components that should be searched. The component must be in canonical form (see DescriptionBase.componentToBase())
     */
    public abstract DescriptionComp[] getCanonicalDescriptionComps();

    public int getPriorityForCanonicalComp(String compName)
    {
        for (DescriptionComp comp : getCanonicalDescriptionComps())
            if (comp.comp.equals(compName))
                return comp.priority;

        return 0;
    }
}
