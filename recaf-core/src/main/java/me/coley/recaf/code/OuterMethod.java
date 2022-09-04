package me.coley.recaf.code;

import javax.annotation.Nullable;

public class OuterMethod {
	private final String className;
	private final String owner;
	@Nullable
	private final String name;
	@Nullable
	private final String descriptor;

	/**
	 * @param owner
	 * 		internal name of the enclosing class of the class.
	 * @param name
	 * 		the name of the method that contains the class, or {@literal null} if the class is
	 * 		not enclosed in a method of its enclosing class.
	 * @param descriptor
	 * 		the descriptor of the method that contains the class, or {@literal null} if
	 * 		the class is not enclosed in a method of its enclosing class.
	 */
	public OuterMethod(String className, String owner, @Nullable String name, @Nullable String descriptor) {
		this.className = className;
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	public String getClassName() {
		return className;
	}

	/**
	 * @return internal name of the enclosing class of the class.
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return the name of the method that contains the class, or {@literal null} if the class is
	 * 		not enclosed in a method of its enclosing class.
	 */
	@Nullable
	public String getName() {
		return name;
	}

	/**
	 * @return the descriptor of the method that contains the class, or {@literal null} if
	 * 		the class is not enclosed in a method of its enclosing class.
	 */
	@Nullable
	public String getDescriptor() {
		return descriptor;
	}
}
