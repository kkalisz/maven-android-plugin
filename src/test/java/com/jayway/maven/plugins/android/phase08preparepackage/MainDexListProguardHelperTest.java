package com.jayway.maven.plugins.android.phase08preparepackage;




import org.junit.Assert;
import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;


public class MainDexListProguardHelperTest
{

    @Test
    public void getValidClassMappingEntryForSimpleClassTest()
    {
        MainDexListProguardHelper mainDexListProguardHelper = new MainDexListProguardHelper();

        String className = "eu.android.test.SomeClass -> eu.android.test.a";

        Map.Entry expectedResult = new AbstractMap.SimpleImmutableEntry( "eu/android/test/SomeClass.class", "eu/android/test/a.class" );

        Map.Entry<String, String> result = mainDexListProguardHelper.getValidClassMappingEntry( className );

        Assert.assertEquals( expectedResult, result );

    }


}