package com.yee.common.valid;

import javax.validation.groups.Default;

/**
 * 校验分组
 * @author YYB
 */
public interface ValidGroup extends Default {

    interface Crud extends ValidGroup {

        interface Create extends Crud {

        }

        interface Update extends Crud {

        }

        interface Query extends Crud {

        }

        interface Delete extends Crud {

        }
    }
}
