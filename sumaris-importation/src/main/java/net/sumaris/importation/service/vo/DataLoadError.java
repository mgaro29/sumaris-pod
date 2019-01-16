package net.sumaris.importation.service.vo;

import net.sumaris.core.dao.technical.schema.SumarisColumnMetadata;
import net.sumaris.core.dao.technical.schema.SumarisTableMetadata;
import net.sumaris.importation.util.csv.FileMessageFormatter;
import net.sumaris.importation.util.csv.FileReader;

public class DataLoadError {

	public enum ErrorType {

		WARNING,
		ERROR,
		FATAL
	}


	public static final class Builder {

		public static Builder create() {
			return new Builder();
		}

		public static Builder create(SumarisTableMetadata table, SumarisColumnMetadata column, int lineNumber, String description) {
			Builder error = new Builder();
			error.setLineNumber(lineNumber);
			error.setColumnName(column != null ? column.getName() : null);
			error.setDescription(FileMessageFormatter.format(table, column, lineNumber, description));
			return error;
		}

		public static Builder create(FileReader reader, SumarisTableMetadata table, SumarisColumnMetadata column, String description) {
			Builder error = new Builder();
			error.setLineNumber(reader.getCurrentLine());
			error.setColumnName(column != null ? column.getName() : null);
			error.setDescription(FileMessageFormatter.format(table, column, reader.getCurrentLine(), description));
			return error;
		}

		protected ErrorType errorType;

		protected String errorCode;

		protected String description;

		protected int lineNumber;

		protected Integer columnNumber;

		protected String columnName;

		public Builder() {

		}

		public Builder setColumnName(String columnName) {
			this.columnName = columnName;
			return this;
		}

		public Builder setColumnNumber(Integer columnNumber) {
			this.columnNumber = columnNumber;
			return this;
		}

		public Builder setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
			return this;
		}

		public Builder setErrorCode(String errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		public Builder setErrorType(ErrorType errorType) {
			this.errorType = errorType;
			return this;
		}

		public Builder setDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder setReader(FileReader reader) {
			this.lineNumber = reader.getCurrentLine();
			return this;
		}

		public DataLoadError build() {
			DataLoadError result = new DataLoadError();
			result.setErrorCode(errorCode);
			result.setErrorType(errorType);
			result.setColumnName(columnName);
			result.setColumnNumber(columnNumber);
			result.setDescription(description);
			result.setLineNumber(lineNumber);
			return result;
		}

	}

	protected String errorCode;

	protected String description;

	protected int lineNumber;

	protected Integer columnNumber;

	protected String columnName;

	public Integer getColumnNumber() {
		return columnNumber;
	}

	public void setColumnNumber(Integer columnNumber) {
		this.columnNumber = columnNumber;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	protected ErrorType errorType;

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public ErrorType getErrorType() {
		return errorType;
	}

	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}

}
