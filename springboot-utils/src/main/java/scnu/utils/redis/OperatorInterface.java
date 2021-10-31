package scnu.utils.redis;

/**
 * @author M1Q84
 * @create 2021年 08月 10日 21:27
 * 数据库操作方法
 * FunctionalInterface注解标记在接口上，“函数式接口”是指仅仅只包含一个抽象方法的接口。
 */
@FunctionalInterface
public interface OperatorInterface<T> {
    T apply();
}
