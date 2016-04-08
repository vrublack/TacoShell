package com.vrublack.nutrition.core.fatsecret;

public class FatSecretException extends Exception
{

    private static final long serialVersionUID = 2401374305831517286L;
    protected int code;       // Internal error code

    public FatSecretException(int code, String message)
    {
        super(message);
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}