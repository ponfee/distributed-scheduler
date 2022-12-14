/* __________              _____                                                *\
** \______   \____   _____/ ____\____   ____    Copyright (c) 2017-2023 Ponfee  **
**  |     ___/  _ \ /    \   __\/ __ \_/ __ \   http://www.ponfee.cn            **
**  |    |  (  <_> )   |  \  | \  ___/\  ___/   Apache License Version 2.0      **
**  |____|   \____/|___|  /__|  \___  >\___  >  http://www.apache.org/licenses/ **
**                      \/          \/     \/                                   **
\*                                                                              */

package cn.ponfee.scheduler.core.enums;

import cn.ponfee.scheduler.common.base.Constants;
import cn.ponfee.scheduler.common.date.CronExpression;
import cn.ponfee.scheduler.common.date.DatePeriods;
import cn.ponfee.scheduler.common.date.Dates;
import cn.ponfee.scheduler.common.util.Enums;
import cn.ponfee.scheduler.common.util.Jsons;
import cn.ponfee.scheduler.common.base.IntValue;
import cn.ponfee.scheduler.core.model.PeriodTriggerConf;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The trigger type enum definition.
 * <p>mapped by sched_job.trigger_type
 *
 * <pre>{@code
 *  org.springframework.scheduling.support.CronExpression.isValidExpression(cronExpression)
 * }</pre>
 *
 * <pre>{@code
 *  new org.springframework.scheduling.support.CronExpression(cronExpression).next(date)
 * }</pre>
 *
 * <pre>{@code
 *  new org.springframework.scheduling.support.CronTrigger(cronExpression).nextExecutionTime(triggerContext);
 * }</pre>
 *
 * @author Ponfee
 * @see org.springframework.scheduling.support.CronTrigger
 * @see org.springframework.scheduling.support.CronExpression
 */
public enum TriggerType implements IntValue<TriggerType> {

    /**
     * Cron expression<br/>
     * Specified date time of cron exp(2021-12-31 23:59:59): 59 59 23 31 12 ? 2021
     */
    CRON(1, "0/10 * * * * ?") {
        @Override
        public boolean isValid(String triggerConf) {
            return CronExpression.isValidExpression(triggerConf);
        }

        @Override
        public Date computeNextFireTime(String triggerConf, Date startTime) {
            try {
                return new CronExpression(triggerConf).getNextValidTimeAfter(startTime);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid cron expression: " + triggerConf, e);
            }
        }

        @Override
        public List<Date> computeNextFireTimes(String triggerConf, Date startTime, int count) {
            List<Date> result = new ArrayList<>(count);
            CronExpression cronExpression;
            try {
                cronExpression = new CronExpression(triggerConf);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Invalid cron expression: " + triggerConf, e);
            }
            while (count-- > 0 && (startTime = cronExpression.getNextValidTimeAfter(startTime)) != null) {
                result.add(startTime);
            }
            return result;
        }
    },

    /**
     * ??????????????????(????????????)???yyyy-MM-dd HH:mm:ss??????
     *
     * @see java.util.Date
     */
    ONCE(2, "2000-01-01 00:00:00") {
        @Override
        public boolean isValid(String triggerConf) {
            try {
                Dates.toDate(triggerConf);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        public Date computeNextFireTime(String triggerConf, Date startTime) {
            try {
                Date dateTime = Dates.toDate(triggerConf);
                return dateTime.after(startTime) ? dateTime : null;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid once date format: " + triggerConf, e);
            }
        }

        @Override
        public List<Date> computeNextFireTimes(String triggerConf, Date startTime, int count) {
            Date nextFireTime = computeNextFireTime(triggerConf, startTime);
            return nextFireTime == null ? Collections.emptyList() : Collections.singletonList(nextFireTime);
        }
    },

    /**
     * ???????????????
     *
     * @see DatePeriods
     */
    PERIOD(3, "{\"period\":\"DAILY\", \"start\":\"2000-01-01 00:00:00\", \"step\":1}") {
        @Override
        public boolean isValid(String triggerConf) {
            try {
                PeriodTriggerConf conf = Jsons.fromJson(triggerConf, PeriodTriggerConf.class);
                return conf != null && conf.isValid();
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        public Date computeNextFireTime(String triggerConf, Date startTime) {
            return getOne(computeNextFireTimes(triggerConf, startTime, 1));
        }

        @Override
        public List<Date> computeNextFireTimes(String triggerConf, Date startTime, int count) {
            PeriodTriggerConf conf;
            try {
                conf = Jsons.fromJson(triggerConf, PeriodTriggerConf.class);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid period config: " + triggerConf, e);
            }
            Assert.isTrue(conf != null && conf.isValid(), "Invalid period config: " + triggerConf);

            DatePeriods period = conf.getPeriod();
            List<Date> result = new ArrayList<>(count);
            Date next;
            if (conf.getStart().after(startTime)) {
                next = conf.getStart();
            } else {
                next = period.next(conf.getStart(), startTime, conf.getStep(), 1).begin();
            }
            result.add(next);
            count--;

            while (count-- > 0) {
                result.add(next = period.next(next, conf.getStep(), 1).begin());
            }
            return result;
        }
    },

    /**
     * ???????????????????????????????????????????????????????????????(trigger_conf????????????job_id?????????????????????)
     */
    DEPEND(4, "3988904755200,3988904755201") {
        @Override
        public boolean isValid(String triggerConf) {
            if (StringUtils.isBlank(triggerConf)) {
                return false;
            }
            try {
                List<Long> list = Arrays.stream(triggerConf.split(Constants.COMMA))
                                        .filter(StringUtils::isNotBlank)
                                        .map(e -> Long.parseLong(e.trim()))
                                        .collect(Collectors.toList());
                return !list.isEmpty() && list.stream().allMatch(e -> e > 0);
            } catch (NumberFormatException ignored) {
                return false;
            }
        }

        @Override
        public Date computeNextFireTime(String triggerConf, Date startTime) {
            throw new UnsupportedOperationException("Trigger type 'DEPEND' unsupported.");
        }

        @Override
        public List<Date> computeNextFireTimes(String triggerConf, Date startTime, int count) {
            throw new UnsupportedOperationException("Trigger type 'DEPEND' unsupported.");
        }
    },

    ;

    private static final Map<Integer, TriggerType> MAPPING = Enums.toMap(TriggerType.class, TriggerType::value);

    private final int value;
    private final String example;

    TriggerType(int value, String example) {
        this.value = value;
        this.example = example;
    }

    @Override
    public int value() {
        return value;
    }

    public String example() {
        return example;
    }

    public abstract boolean isValid(String triggerConf);

    public abstract Date computeNextFireTime(String triggerConf, Date startTime);

    public abstract List<Date> computeNextFireTimes(String triggerConf, Date startTime, int count);

    public static TriggerType of(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("Trigger type cannot be null.");
        }
        TriggerType triggerType = MAPPING.get(value);
        if (triggerType == null) {
            throw new IllegalArgumentException("Invalid trigger type: " + value);
        }
        return triggerType;
    }

    private static <T> T getOne(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        Assert.isTrue(list.size() == 1, "The list expect one size, but actual is " + list.size());
        return list.get(0);
    }
}
