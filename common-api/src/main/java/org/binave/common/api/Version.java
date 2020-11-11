package org.binave.common.api;

/**
 * 用于管理配置一致性
 *
 * @author by bin jin on 2020/9/3 10:04.
 */
public interface Version {

    String getName();

    double getVersion();

    void check() throws RuntimeException;

}
