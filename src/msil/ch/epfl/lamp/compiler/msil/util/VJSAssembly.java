// $Id: VJSAssembly.java 125 2005-04-05 09:16:50Z mihaylov $

package ch.epfl.lamp.compiler.msil.util;

import java.lang.reflect.*;
import java.util.HashMap;

import ch.epfl.lamp.compiler.msil.*;

/**
 * Represents the Microsoft vjslib.dll assembly.
 */
public final class VJSAssembly extends Assembly {

    private VJSAssembly(AssemblyName an) { super(an); }

    protected void loadModules() {
	VJSLIB_DLL.init();
	addModule("vjslib.dll", VJSLIB_DLL);
    }

    public static final Assembly VJSLIB;
    private static final VJSModule VJSLIB_DLL;

    static {
	AssemblyName an = new AssemblyName();
	an.Name = "vjslib";
	an.Version = new Version(1, 0, 3300, 0);
	an.SetPublicKeyToken(new byte [] {(byte)0xB0, 0x3F, 0x5F, 0x7F,
					  0x11, (byte)0xD5, 0x0A, 0x3A});
	VJSLIB = new VJSAssembly(an);
	VJSLIB_DLL = new VJSModule(VJSLIB);
    }


    //##########################################################################

    private static final class VJSModule extends Module {

	VJSModule(Assembly vjslib) {
	    super("vjslib", "vjslib.dll", "vjslib.dll", vjslib);
	}

	void init() {
	    addType("void",    Type.GetType("System.Void"));
	    addType("boolean", Type.GetType("System.Boolean"));
	    addType("char",    Type.GetType("System.Char"));
	    addType("float",   Type.GetType("System.Single"));
	    addType("double",  Type.GetType("System.Double"));
	    addType("byte",    Type.GetType("System.SByte"));
	    addType("short",   Type.GetType("System.Int16"));
	    addType("int",     Type.GetType("System.Int32"));
	    addType("long",    Type.GetType("System.Int64"));
	    addType("java.lang.Object", Type.GetType("System.Object"));
	    addType("java.lang.String", Type.GetType("System.String"));
	}

	public Type[] GetTypes() {
	    throw new RuntimeException("Operation not supported!");
	}

	public Type GetType(String name) {
	    // look it up in the typesMap
	    Type type = super.GetType(name);
	    if (type != null)
		return type;
	    Class clazz = null;
	    try {
		clazz = Class.forName(name);
	    } catch (ClassNotFoundException e) {}
	    return getType(clazz);
	}

	Type getType(Class clazz) {
	    if (clazz == null) return null;
	    Type type = null;
	    if (clazz.isArray()) {
		Type elemType = getType(clazz.getComponentType());
		type = super.GetType(elemType.FullName + "[]");
		if (type != null)
		    return type;
		type = Type.mkArray(elemType, 1);
	    } else {
		type = super.GetType(clazz.getName());
		if (type != null)
		    return type;
		type = new JavaType(clazz);
	    }
	    return addType(type);
	}

	Type[] getTypes(Class [] classes) {
	    if (classes.length == 0) return Type.EmptyTypes;
	    Type [] types = new Type[classes.length];
	    for (int i = 0; i < classes.length; i++)
		types[i] = getType(classes[i]);
	    return types;
	}

    } // class VJSModule

    //##########################################################################

    private static final class JavaType extends Type {

	//######################################################################

	private final Class clazz;

	JavaType(Class clazz, int attrs, String name,
		 Type baseType, Type[] interfaces, Type declType)
	{
	    super(VJSLIB_DLL, attrs, name, baseType, interfaces, declType, 0);
	    this.clazz = clazz;
	}

	JavaType(Class clazz, String name) {
	    super(VJSLIB_DLL,
		  translateTypeAttrs(clazz.getModifiers()),
		  name,
		  VJSLIB_DLL.getType(clazz.getSuperclass()),
		  null,
		  VJSLIB_DLL.getType(clazz.getDeclaringClass()),
		  0);
	    this.clazz = clazz;
	}

	JavaType(Class clazz) {
	    this(clazz, clazz.getName());
	}

	//######################################################################
	// lazy JavaType member loaders

	protected void loadInterfaces() {
	    this.interfaces = VJSLIB_DLL.getTypes(clazz.getInterfaces());
	}

	protected void loadNestedTypes() {
	    Class [] nested = clazz.getDeclaredClasses();
	    this.nestedTypes = new Type[nested.length];
	    for (int i = 0; i < nested.length; i++) {
		String name = nested[i].getName();
		name = name.substring(name.lastIndexOf('$') + 1);
		nestedTypes[i] = new JavaType(nested[i], name);
	    }
	}

	protected void loadFields() {
	    Field [] jfields = clazz.getDeclaredFields();
	    FieldInfo[] fields = new FieldInfo[jfields.length];
	    for (int i = 0; i < jfields.length; i++)
		fields[i] = new JavaFieldInfo(jfields[i]);
	    this.fields = fields;
	}

	protected void loadMethods() {
	    Constructor[] jconstrs = clazz.getDeclaredConstructors();
	    this.constructors = new ConstructorInfo[jconstrs.length];
	    for (int i = 0; i < jconstrs.length; i++)
		this.constructors[i] = new JavaConstructorInfo(jconstrs[i]);

	    Method[] jmethods = clazz.getDeclaredMethods();
	    this.methods = new MethodInfo[jmethods.length];
	    for (int i = 0; i < jmethods.length; i++)
		this.methods[i] = JavaMethodInfo.getMethod(jmethods[i]);
	}

	//######################################################################
	// type resolution methods


	static int translateTypeAttrs(int mods) {
	    int attr = 0;

	    if (Modifier.isInterface(mods))
		attr |= TypeAttributes.Interface;
	    else
		attr |= TypeAttributes.Class;

	    if (Modifier.isAbstract(mods))
		attr |= TypeAttributes.Abstract;

	    if (Modifier.isFinal(mods))
		attr |= TypeAttributes.Sealed;

	    if (Modifier.isPublic(mods))
		attr |= TypeAttributes.Public;
	    else
		attr |= TypeAttributes.NotPublic;

	    return attr;
	}

	//######################################################################
    } // class JavaType

    //##########################################################################

    private static final class JavaFieldInfo extends FieldInfo {
	JavaFieldInfo(Field field) {
	    super(field.getName(),
		  VJSLIB_DLL.getType(field.getDeclaringClass()),
		  translateFieldAttrs(field.getModifiers()),
		  VJSLIB_DLL.getType(field.getType()));
	}

	//translate java modifiers into corresponding .NET Field attributes
	static short translateFieldAttrs(int mods) {
	    short attr = 0;

	    if (Modifier.isFinal(mods))
		attr |= FieldAttributes.InitOnly;

	    if (Modifier.isPublic(mods))
		attr |= FieldAttributes.Public;
	    else if (Modifier.isProtected(mods))
		attr |= FieldAttributes.FamORAssem;
	    else if (Modifier.isPrivate(mods))
		attr |= FieldAttributes.Private;

	    if (Modifier.isStatic(mods))
		attr |= FieldAttributes.Static;

	    return attr;
	}

    } // class JavaFieldInfo

    //##########################################################################

    private static final class JavaConstructorInfo extends ConstructorInfo {
	JavaConstructorInfo(Constructor constr) {
	    super(VJSLIB_DLL.getType(constr.getDeclaringClass()),
		  JavaMethodInfo.translateMethodAttrs(constr.getModifiers()),
		  VJSLIB_DLL.getTypes(constr.getParameterTypes()));
	}
    }

    //##########################################################################

    private static final class JavaMethodInfo extends MethodInfo {

	JavaMethodInfo(String name, Type declType, int attrs,
		       Type returnType, Type[] paramTypes) {
	    super(name, declType, declType, attrs, returnType, paramTypes);
	}

	static MethodInfo getMethod(Method method) {
	    String name = method.getName();
	    if (name.equals("toString"))
		name = "ToString";
	    else if(name.equals("equals"))
		name = "Equals";
	    else if (name.equals("hashCode"))
		name = "GetHashCode";
	    return new JavaMethodInfo
		(name, VJSLIB_DLL.getType(method.getDeclaringClass()),
		 translateMethodAttrs(method.getModifiers()),
		 VJSLIB_DLL.getType(method.getReturnType()),
		 VJSLIB_DLL.getTypes(method.getParameterTypes()));
	}

	static short translateMethodAttrs(int mods) {
	    short attr = 0;

	    if (Modifier.isAbstract(mods))
		attr |= MethodAttributes.Abstract;

	    if (Modifier.isFinal(mods))
		attr |= MethodAttributes.Final;

	    if (Modifier.isPublic(mods))
		attr |= MethodAttributes.Public;
	    else if (Modifier.isProtected(mods))
		attr |= MethodAttributes.FamORAssem;
	    else if (Modifier.isPrivate(mods))
		attr |= MethodAttributes.Private;

	    if (Modifier.isStatic(mods))
		attr |= MethodAttributes.Static;
	    else
		attr |= MethodAttributes.Virtual;

	    //if (Modifier.isSynchronized(mods))
	    //	attr |= MethodAttributes.Synchronized;

	    return attr;
	}
    } // class JavaMethodInfo

    //##########################################################################

} // class VJSAssembly
