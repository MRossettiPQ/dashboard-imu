import {
  Complex,
  divide,
  MathNumericType,
  MathType,
  max,
  mean,
  min,
  multiply,
  pow,
  sqrt,
  std,
  subtract,
} from "mathjs";
import _ from "lodash";

function getColumn<T>(array: T[], column: keyof T): T[keyof T][] {
  return _.map(array, column);
}

function getIndexMinMax(array: number[], value: number): number {
  return array.indexOf(value);
}

function getArraySqrt(array: number[]): number[] {
  return array.map((obj: number) => Number(sqrt(obj)));
}

function getMean(array: number[]): number {
  return mean(array);
}

function getMax(array: number[]): number {
  return max(array);
}

function getMin(array: number[]): number {
  return min(array);
}

function getSqrt(value: number): number {
  return Number(sqrt(value));
}

function getArraySubtract(array: number[], subtractValue: number): number[] {
  return array.map((obj: number) => subtract(obj, subtractValue));
}

function getArrayDivision(array: number[], divisorValue: number): number[] {
  return array.map((obj: number) => divide(obj, divisorValue));
}

function getArrayMultiply(array: number[], multiplyValue: number): number[] {
  return array.map((obj: number) => multiply(obj, multiplyValue));
}

function getArrayPow(array: number[], powValue: number): number[] {
  return array.map((obj: number) => Number(pow(obj, powValue)));
}

function getStDeviation(array: number[]): number {
  return Number(std(array));
}

export {
  getColumn,
  getIndexMinMax,
  getArraySubtract,
  getArraySqrt,
  getMean,
  getMin,
  getMax,
  getArrayMultiply,
  getArrayDivision,
  getArrayPow,
  getStDeviation,
  getSqrt,
};
