package org.binave.common.api;

/**
 * @author by bin jin on 2017/6/13.
 * @since 1.8
 */
public interface SourceDual<Alpha, Beta, Target> {

    Target create(Alpha alpha, Beta beta);

}
