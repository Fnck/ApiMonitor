package com.stubhub.demo.api.monitor.aether.util;

import java.io.PrintStream;

import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.graph.DependencyVisitor;

public class ConsoleDependencyGraphDumper
implements DependencyVisitor
{

private PrintStream out;

private String currentIndent = "";

public ConsoleDependencyGraphDumper()
{
    this( null );
}

public ConsoleDependencyGraphDumper( PrintStream out )
{
    this.out = ( out != null ) ? out : System.out;
}

public boolean visitEnter( DependencyNode node )
{
    out.println( currentIndent + node );
    if ( currentIndent.length() <= 0 )
    {
        currentIndent = "+- ";
    }
    else
    {
        currentIndent = "|  " + currentIndent;
    }
    return true;
}

public boolean visitLeave( DependencyNode node )
{
    currentIndent = currentIndent.substring( 3, currentIndent.length() );
    return true;
}

}
