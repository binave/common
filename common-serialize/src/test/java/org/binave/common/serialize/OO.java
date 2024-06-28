package org.binave.common.serialize;

/**
 * For ASM
 *
 * @author by bin jin on 2017/5/2.
 */
abstract class OO<E> {

    private E e;

    private long id;

    public E getE() {
        return e;
    }

    public void setE(E e) {
        this.e = e;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
