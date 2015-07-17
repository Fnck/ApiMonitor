package com.stubhub.demo.api.monitor.entity;

import java.util.Properties;

public class WadlGeneratorDescription
{
  private String _className;
  private Properties _properties;

  public String getClassName()
  {
    return this._className;
  }

  public void setClassName(String className)
  {
    this._className = className;
  }

  public Properties getProperties()
  {
    return this._properties;
  }

  public void setProperties(Properties properties)
  {
    this._properties = properties;
  }
}
