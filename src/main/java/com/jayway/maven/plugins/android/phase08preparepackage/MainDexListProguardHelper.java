package com.jayway.maven.plugins.android.phase08preparepackage;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by kkalisz on 2014-08-19.
 */
public class MainDexListProguardHelper
{
    /**
     * @param mainDexClassFile - file with original list of classes that should be
     *                             located in main dex
     * @return  file with list of classes that should be in main dex, mapped from proguard mapping
     */
    public File updateMainDexListAfterProguard( File parsedOutputDirectory, File mainDexClassFile,
                                                  File proguardUpdatedMainDexFileList )
            throws MojoExecutionException
    {

        File proguardMapping = new File( parsedOutputDirectory, "mapping.txt" );

        if ( !proguardMapping.exists() )
        {
            // if there is no proguard file we return old file path
            return mainDexClassFile;
        }

        Set<String> mainDexClasses = extractMainDexClasses( mainDexClassFile );

        Map<String, String> proguardMap = extractProguardMapping( proguardMapping );

        Set<String> remappedMainDexClasses = updateMainDexClassesWithMapping( mainDexClasses, proguardMap );

        saveMainDexClassesToFile( remappedMainDexClasses, proguardUpdatedMainDexFileList );

        return proguardUpdatedMainDexFileList;

    }

    private void saveMainDexClassesToFile( Set<String> remapedMainDexClasses, File remapedMainDexClassesFile )
            throws MojoExecutionException
    {
        try
        {
            FileWriter outputStream = new FileWriter( remapedMainDexClassesFile );

            String newLine = System.getProperty( "line.separator" );

            for ( String className : remapedMainDexClasses )
            {
                outputStream.write( String.format( "%s%s", className, newLine ) );
            }
            outputStream.close();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException
                    ( String.format( "Exception during saving %s", remapedMainDexClassesFile.getName() ), e );
        }

    }

    private Set<String> updateMainDexClassesWithMapping( Set<String> mainDexClasses, Map<String, String> proguardMap )
    {
        Set<String> classNamesCopy = new HashSet<String>( mainDexClasses );
        for ( String className : mainDexClasses )
        {
            if ( proguardMap.containsKey( className ) )
            {
                classNamesCopy.remove( className );
                classNamesCopy.add( proguardMap.get( className ) );
            }
        }
        return classNamesCopy;
    }


    private Map<String, String> extractProguardMapping( File proguardMapping ) throws MojoExecutionException
    {
        Map<String, String> proguardMap = new HashMap<String, String>();
        try
        {
            BufferedReader mappingReader = new BufferedReader( new FileReader( proguardMapping ) );
            String lineEntry = mappingReader.readLine();
            while ( lineEntry != null )
            {
                Map.Entry<String, String> entry = getValidClassMappingEntry( lineEntry );
                if ( entry != null )
                {
                    proguardMap.put( entry.getKey(), entry.getValue() );
                }
                lineEntry = mappingReader.readLine();
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Exception during reading proguard mapping", e );
        }
        return proguardMap;
    }

    private Set<String> extractMainDexClasses( File mainDexClassFile ) throws MojoExecutionException
    {
        Set<String> mainDexClasses = new HashSet<String>();
        try
        {
            BufferedReader mainDexClassReader = new BufferedReader( new FileReader( mainDexClassFile ) );
            String lineEntry = mainDexClassReader.readLine();
            while ( lineEntry != null )
            {
                mainDexClasses.add( lineEntry );
                lineEntry = mainDexClassReader.readLine();
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException
                    ( String.format( "Exception during reading %s", mainDexClassFile.getName() ), e );
        }
        return mainDexClasses;
    }

    protected Map.Entry<String, String> getValidClassMappingEntry( String valueToCheck )
    {
        if ( isValidClassName( valueToCheck ) )
        {
            String classFilePathEntry = valueToCheck.replaceAll( "\\.", "/" );
            String trimmedValue = classFilePathEntry.replaceAll( "\\s+", "" );

            final String splittedValue[] = trimmedValue.split( "->" );
            addClassSuffix( splittedValue );
            return new AbstractMap.SimpleImmutableEntry<String, String>( splittedValue[0], splittedValue[1] );
        }
        return null;
    }

    /**
     * functions add ".class" suffix for each String in array
     * @param array Strings to modify
     */
    private void addClassSuffix( String[] array )
    {
        for ( int i = 0; i < array.length; i++ )
        {
            array[i] = array[i] + ".class";
        }
    }

    private boolean isValidClassName( String line )
    {
        if ( StringUtils.isEmpty( line ) )
        {
            return false;
        }
        if ( Character.isWhitespace( line.charAt( 0 ) ) )
        {
            return false;
        }
        return true;
    }

}
