import java.util.concurrent.ConcurrentHashMap;

public class CHM {


    public static void main(String[] args) {

        // 有点：多线程情况下，安全 高效

        /*
        * 面试题：如何使用这个集合：根据集合的功能，建议是给一个初始容量 ，扩容耗时
        * */
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();

        /*
        * 如果new ConcurrentHashMap(32); 1,7  是创建了32的空间
        * 而在1.8之后 是创建了64个空间
        *public ConcurrentHashMap(int initialCapacity) {
            if (initialCapacity < 0)
                throw new IllegalArgumentException();
            int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                       MAXIMUM_CAPACITY :
                       // 这里是 1.5倍的initialCapacity(32) + 1
                       // tableSizeFor()保证是2的n次方 ，所以是64
                       tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
            this.sizeCtl = cap;
            // sizeCtl 很重要
            *   （1）sizeCtl 为0，代表数组还未初始化，且数组的初始容量为16
            *   （2）sizeCtl大于0，如果数组还未初始化，那么记录的信息是他的初始化容量，如果数组已经被初始化了，那么记录的是数组扩容的阈值。
            *       扩容阈值的计算方法：数组的初始容量 * 扩容因子0.75
            *   （3）sizeCtl为-1，表示正在进行初始化，是为了多线程的考虑，为-1时，其他线程不能对其初始化
            *   （4）sizeCtl为负数，但是并不是-1，-(1+n)，表示的是此时有n个线程正在共同完成数组的扩容操作
        }
        * */

        /*
        final V putVal(K key, V value, boolean onlyIfAbsent) {
            if (key == null || value == null) throw new NullPointerException(); 不允许空key和空value
            int hash = spread(key.hashCode()); // 根据key来求hash值，该值一定是正数，方便后面添加元素判断该节点的类型

            // static final int spread(int h) {
            //     return (h ^ (h >>> 16)) & HASH_BITS;
            // } HASH_BITS = 0x7fffffff = 01111111111111 第一位是0，&操作是全1位1，最高为一定是0

            int binCount = 0;
            for (Node<K,V>[] tab = table;;) { // table是当前集合对象中真正存储数据的数组
                Node<K,V> f; int n, i, fh;
                if (tab == null || (n = tab.length) == 0) 判断tab是否为null
                    tab = initTable(); 对数组进行初始化
                                |--------------------------->
                               | private final Node<K,V>[] initTable() {
                               |    Node<K,V>[] tab; int sc;
                               |     while ((tab = table) == null || tab.length == 0) {
                               |         // 如果sizeCtl的值小于0，说明此时正在初始化，让出cpu
                               |         if ((sc = sizeCtl) < 0)
                               |             Thread.yield(); // lost initialization race; just spin
                               |         // 判断sc是不是跟原本的SIZECTl相等，
                               |         else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {  // 如果设置失败证明有别的线程正在进行该操作
                               |             try {
                               |                 if ((tab = table) == null || tab.length == 0) {
                               |                     int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                               |                     @SuppressWarnings("unchecked")
                               |                     Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                               |                     table = tab = nt;
                               |                     sc = n - (n >>> 2);
                               |                 }
                               |             } finally {
                               |                 sizeCtl = sc;
                               |             }
                               |             break;
                               |         }
                               |     }
                               |    return tab;
                               | }

                // tabAt()通过索引去取tab中的值，(n-1) & hash就是索引
                else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) { f==null表示在这个索引位置没有值
                    // 直接添加数据
                    if (casTabAt(tab, i, null,
                                 new Node<K,V>(hash, key, value, null)))
                        break;                   // no lock when adding to empty bin
                }
                else if ((fh = f.hash) == MOVED)  // 这时f是有数据的，拿到他的hash值判断==Moved（多线程协助扩容）
                    tab = helpTransfer(tab, f);
                else {  // 在这个索引位置有元素的情况下添加元素
                    V oldVal = null;
                    synchronized (f) { // 对这个索引位置的元素上锁，其他位置的元素添加操作不受影响
                        if (tabAt(tab, i) == f) {
                            if (fh >= 0) { // 表示的是一个普通的链表结构，上文说的>0为链表
                                binCount = 1;
                                for (Node<K,V> e = f;; ++binCount) {
                                    K ek;
                                    if (e.hash == hash &&
                                        ((ek = e.key) == key ||
                                         (ek != null && key.equals(ek)))) {
                                        oldVal = e.val;
                                        if (!onlyIfAbsent)
                                            e.val = value;
                                        break;
                                    }
                                    Node<K,V> pred = e;
                                    if ((e = e.next) == null) {
                                        pred.next = new Node<K,V>(hash, key,
                                                                  value, null);
                                        break;
                                    }
                                }
                            }
                            else if (f instanceof TreeBin) {
                                Node<K,V> p;
                                binCount = 2;
                                if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                               value)) != null) {
                                    oldVal = p.val;
                                    if (!onlyIfAbsent)
                                        p.val = value;
                                }
                            }
                        }
                    }
                    if (binCount != 0) {
                        if (binCount >= TREEIFY_THRESHOLD)  // TREEIFY_THRESHOLD=8
                            treeifyBin(tab, i); // 考虑要不要吧链表变成树，但是数组的长度小于64则不会变成树
                        if (oldVal != null)
                            return oldVal;
                        break;
                    }
                }
            }
            addCount(1L, binCount); // 这个方法做了两件事：1，维护的集合的长度 2，判断集合的长度是不是达到了扩容阈值，如果是则扩容
                    |   // 集合对象里面有一个基础变量BaseCount，对集合添加元素后对集合长度进行维护，会对baseCount++，如果同时有多个线程添加元素，会产生竞争
                           所以，在集合内部又加了数组，在有多个线程维护时，对数组内部的value维护，最后的长度就是baseCount+value的值

                        private final void addCount(long x, int check) {
                    |        CounterCell[] as; long b, s; // CounterCell就是那个数组。默认值为null
                    |        if ((as = counterCells) != null ||
                    |            !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
                    |            // 多个线程操作才进入这个判断中
                    |            CounterCell a; long v; int m;
                    |            boolean uncontended = true;
                    |            if (as == null || (m = as.length - 1) < 0 ||
                    |                (a = as[ThreadLocalRandom.getProbe() & m]) == null ||
                    |                !(uncontended =
                    |                  U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
                    |                fullAddCount(x, uncontended);
                    |                return;
                    |            }
                    |            if (check <= 1)
                    |                return;
                    |            s = sumCount();
                    |        }
                    |        if (check >= 0) {
                    |            Node<K,V>[] tab, nt; int n, sc;
                    |            while (s >= (long)(sc = sizeCtl) && (tab = table) != null &&
                    |                   (n = tab.length) < MAXIMUM_CAPACITY) {
                    |                int rs = resizeStamp(n);
                    |                if (sc < 0) {
                    |                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                    |                        sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                    |                        transferIndex <= 0)
                    |                        break;
                    |                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                    |                        transfer(tab, nt);
                    |                }
                    |                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                    |                                             (rs << RESIZE_STAMP_SHIFT) + 2))
                    |                    transfer(tab, null);
                    |                s = sumCount();
                    |            }
                    |        }
                    |    }

            return null;
        }




        private final void fullAddCount(long x, boolean wasUncontended) {
            int h;
            if ((h = ThreadLocalRandom.getProbe()) == 0) {
                ThreadLocalRandom.localInit();      // force initialization
                h = ThreadLocalRandom.getProbe();
                wasUncontended = true;
            } // 拿到随机值计算数组的角标
            boolean collide = false;                // True if last slot nonempty
            for (;;) {
                CounterCell[] as; CounterCell a; int n; long v;
                if ((as = counterCells) != null && (n = as.length) > 0) {
                    if ((a = as[(n - 1) & h]) == null) {
                        if (cellsBusy == 0) {            // Try to attach new Cell
                            CounterCell r = new CounterCell(x); // Optimistic create
                            if (cellsBusy == 0 &&
                                U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                                boolean created = false;
                                try {               // Recheck under lock
                                    CounterCell[] rs; int m, j;
                                    if ((rs = counterCells) != null &&
                                        (m = rs.length) > 0 &&
                                        rs[j = (m - 1) & h] == null) {
                                        rs[j] = r;
                                        created = true;
                                    }
                                } finally {
                                    cellsBusy = 0;
                                }
                                if (created)
                                    break;
                                continue;           // Slot is now non-empty
                            }
                        }
                        collide = false;
                    }
                    else if (!wasUncontended)       // CAS already known to fail
                        wasUncontended = true;      // Continue after rehash
                    else if (U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))
                        break;
                    else if (counterCells != as || n >= NCPU)
                        collide = false;            // At max size or stale
                    else if (!collide)
                        collide = true;
                    else if (cellsBusy == 0 &&
                             U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                        try {
                            if (counterCells == as) {// Expand table unless stale
                                CounterCell[] rs = new CounterCell[n << 1];
                                for (int i = 0; i < n; ++i)
                                    rs[i] = as[i];
                                counterCells = rs;
                            }
                        } finally {
                            cellsBusy = 0;
                        }
                        collide = false;
                        continue;                   // Retry with expanded table
                    }
                    h = ThreadLocalRandom.advanceProbe(h);
                }
                else if (cellsBusy == 0 && counterCells == as &&
                         U.compareAndSwapInt(this, CELLSBUSY, 0, 1)) {
                    boolean init = false;
                    try {                           // Initialize table
                        if (counterCells == as) {
                            CounterCell[] rs = new CounterCell[2];
                            rs[h & 1] = new CounterCell(x);
                            counterCells = rs;
                            init = true;
                        }
                    } finally {
                        cellsBusy = 0;
                    }
                    if (init)
                        break;
                }
                else if (U.compareAndSwapLong(this, BASECOUNT, v = baseCount, v + x))
                    break;                          // Fall back on using base
            }
        }
        */



    }
}
