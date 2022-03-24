import java.util.HashSet;

public class HashSetSource {

    public static void main(String[] args) {



        HashSet set = new HashSet();
        set.add("java");
        set.add("php");
        set.add("java");

        System.out.println("set=" + set);

        /*
        1. 执行HashSet set = new HashSet();
            HashSet构造方法执行的就是HashMap
        public HashSet() {
            map = new HashMap<>();
        }

        2. 执行set.add("java");

        public boolean add(E e) {   e:java
            return map.put(e, PRESENT)==null;
            // private static final Object PRESENT = new Object();
            // 起到占位的作用
        }

        3.执行put()方法,该方法会执行Hash(key)得到key对应的hash值，hash值并不是完全等价于hashcode
        public V put(K key, V value) {
            // key = "java" value = Object@535 其实就是上面的add方法传进来的PRESENT，是共享的
            return putVal(hash(key), key, value, false, true);
        }

        4. 执行putVal()方法
        final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
            Node<K,V>[] tab; Node<K,V> p; int n, i; // 定义了辅助变量

            // table就是hashmap的一个数组，类型是Node[]
            // if语句表示的就是如果当前table是null或者大小等于0
            // 就是第一次扩容，到16个空间
            if ((tab = table) == null || (n = tab.length) == 0)
                n = (tab = resize()).length;  // 变成16了

            // (1)根据key，得到hash去计算该key应该存放到table表的哪个索引位置
            // 并把这个位置的对象赋值给p
            // (2)判断p是否=null
            // (2,1) 如果p==null，表示还没有存放元素，就创建一个Node(key="java", value=PRESENT)
            // 放在该位置tab[i] = newNode(hash, key, value, null);
            if ((p = tab[i = (n - 1) & hash]) == null)
                tab[i] = newNode(hash, key, value, null);
            else {
                // 开发技巧：在需要的局部变量的地方，再创建变量。
                Node<K,V> e; K k; // 辅助变量

                // 如果当前索引位置对应的链表的第一个元素和准备添加的key的hash值一样
                // 并且满足
                //       （1）准备加入的key和p指向的node节点的key是同一个对象
                //       （2）不是同一个对象，但是内容相同
                // 则不能添加
                if (p.hash == hash && //
                    ((k = p.key) == key || (key != null && key.equals(k))))
                    e = p;
                // 判断p是不是一颗红黑树
                // 如果是一颗红黑树，就调用putTreeVal，来进行添加
                else if (p instanceof TreeNode)
                    e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
                else {
                    // 如果table对应的索引位置已经是一个链表了，就用for循环比较
                    // （1）依次和该链表的每一个元素比较后，都不相同，则添加到该链表的最后
                    //  (2)依次和该链表的每一个元素比较中，如果有相同的情况，直接break
                    for (int binCount = 0; ; ++binCount) {  // 死循环
                        if ((e = p.next) == null) { （1）
                            p.next = newNode(hash, key, value, null);
                            if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                                treeifyBin(tab, hash);
                            break;
                        }
                        if (e.hash == hash && （2）
                            ((k = e.key) == key || (key != null && key.equals(k))))
                            break;
                        p = e;
                    }
                }
                if (e != null) { // existing mapping for key
                    V oldValue = e.value;
                    if (!onlyIfAbsent || oldValue == null)
                        e.value = value;
                    afterNodeAccess(e);
                    return oldValue;
                }
            }
            ++modCount;
            if (++size > threshold)
                resize();
            afterNodeInsertion(evict); // 预留了一个空方法，是为了给HashMap的子类来实现的
            return null; // 返回为null 代表添加成功了
        }
        */
    }
}
