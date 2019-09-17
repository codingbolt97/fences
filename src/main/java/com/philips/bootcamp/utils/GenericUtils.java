package com.philips.bootcamp.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenericUtils {
    
    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> c) {
      if (clazz == null || c == null) throw new RuntimeException("[ERROR] Null argument(s)");
      
      List<T> r = new ArrayList<T>(c.size());
      for(Object o: c)
        r.add(clazz.cast(o));
      return r;
    }
}