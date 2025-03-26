// import { intersection, toNumber } from "lodash";
// import { Model } from "sequelize-typescript";
//
// interface PaginationParams {
//   rpp: number;
//   page: number;
//   field: string;
//   order: any[];
//   where: any;
//   options: any;
// }
//
// interface Pagination<T> {
//   entity: T;
//   params?: PaginationParams;
// }
//
// export interface PaginationResult<T> {
//   rpp: number;
//   page: number;
//   list: T[];
//   count: number;
// }
//
// export class PaginationUtil implements Pagination<any> {
//   entity?: Model;
//   params?: PaginationParams;
//
//   constructor(entity: Model) {
//     this.entity = entity;
//   }
//
//   // mergeFields(entity: Model, strFields: string) {
//   //   const allFieldsOfEntity = Object.keys(entity.getAttributes());
//   //   // CASO REQUEST NÃO TENHA CAMPOS ESPECIFICOS, PEGAR TODOS CAMPOS DA ENTIDADE
//   //   if (!strFields.length) {
//   //     return allFieldsOfEntity;
//   //   }
//   //   let fields = [];
//   //   // SPLITA CAMPOS REQUISITADOS NA REQUEST
//   //   if (strFields.length) {
//   //     fields = strFields?.split(",");
//   //   }
//   //   // RETORNA CAMPOS SOLICITADOS QUE ESTEJAM NA ENTIDADE
//   //   const resultFields = intersection(allFieldsOfEntity, fields);
//   //   if (resultFields.length) {
//   //     return resultFields;
//   //   }
//   //   // CASO NENHUM CAMPO SOLICITADO SEJA VALIDO RETORNAR TODOS
//   //   return allFieldsOfEntity;
//   // }
//   //
//   // getOffset(page: number, rpp: number): number {
//   //   return page * rpp;
//   // }
//   //
//   // getEndPosition(page: number, rpp: number, resultLength: number): number {
//   //   if (page > 0) {
//   //     return page * rpp + resultLength;
//   //   }
//   //   return resultLength;
//   // }
//   //
//   // getMaxPages(count, rpp): number {
//   //   const maxPages = count / rpp;
//   //   if (maxPages <= 1) {
//   //     return count / rpp;
//   //   }
//   //   return Math.ceil(maxPages);
//   // }
//   //
//   // paginatedResult({ rpp = 10, page = 0, field = "", order = [["id", "ASC"]], where = null, options } = {}): PaginationResult<T> {
//   //   // TRANSFORMAR PARAMS EM NUMERO
//   //   const actualPage = toNumber(page);
//   //   const actualRpp = toNumber(rpp);
//   //
//   //   // OFFSET DA QUERY NO BANCO
//   //   const offset = this.getOffset(actualPage, actualRpp);
//   //
//   //   // CAMPOS REQUISITADOS NA QUERY
//   //   const fields = this.mergeFields(this.entity, field);
//   //
//   //   // QUERY NO BANCO
//   //   const result = await this.entity?.findAndCountAll({
//   //     where,
//   //     order,
//   //     attributes: fields,
//   //     //limit: actualRpp, TODO tirar comentario para paginação
//   //     //offset, TODO tirar comentario para paginação
//   //     ...options,
//   //   });
//   //
//   //   const count = result.count;
//   //   // POSIÇÃO DO RESULTADO CONSIDERANDO OFFSET
//   //   // const endPosition = this.getEndPosition(actualPage, actualRpp, result.rows.length);
//   //
//   //   // RESULTADO DA PAGINAÇÃO
//   //   return {
//   //     list: result.rows,
//   //     count,
//   //     rpp,
//   //     page,
//   //     // endPosition,
//   //     // more: result.count > endPosition,
//   //     // maxPages: this.getMaxPages(count, rpp, result.rows.length),
//   //   };
//   // }
//
//   // GetWhere(cond, where) {
//   //   if (cond) {
//   //     return {
//   //       ...where,
//   //     };
//   //   }
//   //   return {};
//   // }
//
//   // mergeOrder(entity, orderBy) {
//   //   const allFieldsOfEntity = Object.keys(entity.getAttributes());
//   //   // CASO REQUEST NÃO TENHA CAMPOS ESPECIFICOS, ORDENAR APENAS PELO ID
//   //   if (!orderBy.length) {
//   //     return [["id", "ASC"]];
//   //   }
//   //   // RETORNA CAMPOS DE ORDENAÇÃO VALIDOS
//   //   const resultOrder = orderBy.filter((o) => allFieldsOfEntity.includes(o[0]));
//   //   if (resultOrder.length) {
//   //     return orderBy.map((o) => o);
//   //   }
//   //   // CASO REQUEST NÃO TENHA CAMPOS VALIDOS, ORDENAR APENAS PELO ID
//   //   return [["id", "ASC"]];
//   // }
// }
