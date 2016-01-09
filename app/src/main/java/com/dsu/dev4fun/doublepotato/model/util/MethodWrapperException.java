package com.dsu.dev4fun.doublepotato.model.util;

public interface MethodWrapperException<T, E extends Throwable> {
    public void execute(T params) throws E;

}
