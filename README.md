# springbootredisdemo
springboot+redis整合

redis提供五种数据类型：string，hash，list，set及zset(sorted set)

string（字符串）
string是最简单的类型，你可以理解成与Memcached一模一样的类型，一个key对应一个value，
redis采用结构sdshdr和sds封装了字符串，字符串相关的操作实现在源文件sds.h/sds.c中。
数据结构定义如下：
typedefchar*sds;
structsdshdr{
longlen;
longfree;
charbuf[];
};

list(双向链表)
list是一个链表结构，主要功能是push、pop、获取一个范围的所有值等等。操作中key理解为链表的名字。
对list的定义和实现在源文件adlist.h/adlist.c，相关的数据结构定义如下：

//list迭代器
typedefstructlistIter{
listNode*next;
intdirection;
}listIter;
//list数据结构
typedefstructlist{
listNode*head;
listNode*tail;
void*(*dup)(void*ptr);
void(*free)(void*ptr);
int(*match)(void*ptr,void*key);
unsignedintlen;
listIteriter;
}list;

dict(hash表)
set是集合，和我们数学中的集合概念相似，对集合的操作有添加删除元素，有对多个集合求交并差等操作。操作中key理解为集合的名字。
在源文件dict.h/dict.c中实现了hashtable的操作，数据结构的定义如下：

//dict中的元素项
typedefstructdictEntry{
void*key;
void*val;
structdictEntry*next;
}dictEntry;
//dict相关配置函数
typedefstructdictType{
unsignedint(*hashFunction)(constvoid*key);
void*(*keyDup)(void*privdata,constvoid*key);
void*(*valDup)(void*privdata,constvoid*obj);
int(*keyCompare)(void*privdata,constvoid*key1,constvoid*key2);
void(*keyDestructor)(void*privdata,void*key);
void(*valDestructor)(void*privdata,void*obj);
}dictType;
//dict定义
typedefstructdict{
dictEntry**table;
dictType*type;
unsignedlongsize;
unsignedlongsizemask;
unsignedlongused;
void*privdata;
}dict;
//dict迭代器
typedefstructdictIterator{
dict*ht;
intindex;
dictEntry*entry,*nextEntry;
}dictIterator;
dict中table为dictEntry指针的数组，数组中每个成员为hash值相同元素的单向链表。set是在dict的基础上实现的，指定了key的比较函数为dictEncObjKeyCompare，若key相等则不再插入。

zset(排序set)
zset是set的一个升级版本，他在set的基础上增加了一个顺序属性，这一属性在添加修改元素的时候可以指定，每次指定后，zset会自动重新按新的值调整顺序。可以理解了有两列的mysql表，一列存value，一列存顺序。操作中key理解为zset的名字。

typedefstructzskiplistNode{
structzskiplistNode**forward;
structzskiplistNode*backward;
doublescore;
robj*obj;
}zskiplistNode;
typedefstructzskiplist{
structzskiplistNode*header,*tail;
unsignedlonglength;
intlevel;
}zskiplist;
typedefstructzset{
dict*dict;
zskiplist*zsl;
}zset;
zset利用dict维护key -> value的映射关系，用zsl(zskiplist)保存value的有序关系。zsl实际是叉数
不稳定的多叉树，每条链上的元素从根节点到叶子节点保持升序排序。

redis安装很简单，大家可以进行百度；在下相信大家有比我更强的检索能力。
