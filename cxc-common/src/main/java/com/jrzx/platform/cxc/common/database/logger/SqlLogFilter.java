/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & www.dreamlu.net).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jrzx.platform.cxc.common.database.logger;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.JdbcParameter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.util.StringUtils;
import com.jr.platform.database.config.MybatisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 打印可执行的 sql 日志
 * 参考：https://jfinal.com/share/2204

 */
@Slf4j
@RequiredArgsConstructor
public class SqlLogFilter extends FilterEventAdapter {
	private static final SQLUtils.FormatOption FORMAT_OPTION = new SQLUtils.FormatOption(false, false);
	private final MybatisProperties properties;

	@Override
	protected void statementExecuteBefore(StatementProxy statement, String sql) {
		statement.setLastExecuteStartNano();
	}

	@Override
	protected void statementExecuteBatchBefore(StatementProxy statement) {
		statement.setLastExecuteStartNano();
	}

	@Override
	protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
		statement.setLastExecuteStartNano();
	}

	@Override
	protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
		statement.setLastExecuteStartNano();
	}

	@Override
	protected void statementExecuteAfter(StatementProxy statement, String sql, boolean firstResult) {
		statement.setLastExecuteTimeNano();
	}

	@Override
	protected void statementExecuteBatchAfter(StatementProxy statement, int[] result) {
		statement.setLastExecuteTimeNano();
	}

	@Override
	protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
		statement.setLastExecuteTimeNano();
	}

	@Override
	protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
		statement.setLastExecuteTimeNano();
	}

	@Override
	public void statement_close(FilterChain chain, StatementProxy statement) throws SQLException {
		// 支持动态开启
		if (!properties.isSql()) {
			return;
		}
		// 是否开启调试
		if (!log.isInfoEnabled()) {
			return;
		}
		// 打印可执行的 sql
		String sql = statement.getBatchSql();
		// sql 为空直接返回
		if (StringUtils.isEmpty(sql)) {
			return;
		}
		int parametersSize = statement.getParametersSize();
		List<Object> parameters = new ArrayList<>(parametersSize);
		for (int i = 0; i < parametersSize; ++i) {
			JdbcParameter jdbcParam = statement.getParameter(i);
			parameters.add(jdbcParam != null ? jdbcParam.getValue() : null);
		}
		//可以使用数据库类型常量
		String dbType = statement.getConnectionProxy().getDirectDataSource().getDbType();
		String formattedSql = SQLUtils.format(sql, DbType.of(dbType), parameters, FORMAT_OPTION);
		printSql(formattedSql, statement);
	}

	private static void printSql(String sql, StatementProxy statement) {
		// 打印 sql
		String sqlLogger = "\n\n==============  Sql Start  ==============" +
			"\nExecute SQL ：{}" +
			"\nExecute Time：{}" +
			"\n==============  Sql  End   ==============\n";
		log.info(sqlLogger, sql.trim(), statement.getLastExecuteTimeNano());
	}

}
