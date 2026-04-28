import _ from 'lodash';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';
import isSameOrAfter from 'dayjs/plugin/isSameOrAfter';

dayjs.extend(customParseFormat);
dayjs.extend(isSameOrAfter);

class ValidatorsUtils {
  cnpj(cnpj: string): boolean | string {
    const cnpjNumbers: string = cnpj?.replace(/\D/g, '');

    if (
      cnpjNumbers?.length !== 14 ||
      cnpjNumbers?.split('').every((digit) => digit === cnpjNumbers[0])
    ) {
      return 'CNPJ Inválido';
    }

    const cnpjArray: number[] = cnpjNumbers.split('').map(Number);
    let sum = 0;
    let position = 5;

    for (let i = 0; i < 12; i++) {
      const digit = cnpjArray[i];
      if (digit) {
        sum += digit * position;
        position = position === 2 ? 9 : position - 1;
      }
    }

    let mod: number = sum % 11;
    const checkDigit1: number = mod < 2 ? 0 : 11 - mod;

    if (checkDigit1 !== cnpjArray[12]) {
      return 'CNPJ Inválido';
    }

    sum = 0;
    position = 6;

    for (let i = 0; i < 13; i++) {
      const digit = cnpjArray[i];
      if (digit) {
        sum += digit * position;
        position = position === 2 ? 9 : position - 1;
      }
    }

    mod = sum % 11;
    const checkDigit2: number = mod < 2 ? 0 : 11 - mod;

    return checkDigit2 === cnpjArray[13] ? true : 'CNPJ Inválido';
  }

  cpf(cpf: string): boolean | string {
    const cpfNumbers: string = cpf?.replace(/\D/g, '');

    if (
      cpfNumbers?.length !== 11 ||
      cpfNumbers?.split('').every((digit) => digit === cpfNumbers[0])
    ) {
      return 'CPF Inválido';
    }

    let sum = 0;
    let weight = 10;

    for (let i = 0; i < 9; i++) {
      const digit = cpfNumbers[i];
      if (digit) {
        sum += parseInt(digit) * weight;
        weight--;
      }
    }

    const digitVerif1: number = sum % 11 < 2 ? 0 : 11 - (sum % 11);

    let digit = cpfNumbers[9];
    if (digit == undefined || digitVerif1 !== parseInt(digit)) {
      return 'CPF Inválido';
    }

    sum = 0;
    weight = 11;
    for (let i = 0; i < 10; i++) {
      const digit = cpfNumbers[i];
      if (digit) {
        sum += parseInt(digit) * weight;
        weight--;
      }
    }

    const digitVerif2: number = sum % 11 < 2 ? 0 : 11 - (sum % 11);
    digit = cpfNumbers[10];
    return digit && digitVerif2 === parseInt(digit) ? true : 'CPF Inválido';
  }

  email(email: string): boolean | string {
    if (!email) {
      return true;
    }

    const msgError = 'E-mail inválido';
    const indexOf = email.indexOf('@');

    if (indexOf === -1) {
      return msgError;
    }

    const subEmail = email.substring(indexOf + 1);

    if (!subEmail.length) {
      return msgError;
    }

    return true;
  }

  notBlank(val: unknown): boolean | string {
    if (typeof val == 'number' || !_.isEmpty(val)) {
      return true;
    }
    return 'Preenchimento obrigatório';
  }

  alfanumeric(val: string | number): boolean | string {
    const regex = /^[A-Za-z]\d+$/;
    let str = '';
    if (typeof val == 'number') {
      str = val.toString();
    } else {
      str = val;
    }

    return regex.test(str.toString()) || 'A string não é alfanumérica no formato esperado.';
  }

  after(startDate?: string): (endDate: string) => boolean | string {
    return function date(endDate: string): boolean | string {
      if (startDate == null || endDate == null || endDate?.length == 0) {
        return true;
      }

      const start = dayjs(startDate, 'DD/MM/YYYY', true);
      const end = dayjs(endDate, 'DD/MM/YYYY', true);

      return end.isSameOrAfter(start) || 'Data final inválida';
    };
  }

  date(format: string): (params: string) => boolean | string {
    return function date(val: string): boolean | string {
      if (!val) {
        return true;
      }

      if (val.length < format.length) {
        return 'Data inválida';
      }

      const date = dayjs(val, format).format('YYYY-MM-DD[T]HH:mm:ss');

      if (date === 'Invalid date') {
        return 'Data inválida';
      }

      return true;
    };
  }

  minLength(minLength: number) {
    return function (val: string) {
      return !val || (val + '').length >= minLength || `Tamanho mínimo permitido é ${minLength} .`;
    };
  }

  passwordEqual(equalFor: unknown): (params: unknown) => boolean | string {
    return function equal(value: unknown): boolean | string {
      return !_.isEqual(value, equalFor) ? 'As senhas informadas são diferentes' : true;
    };
  }

  equal(equalFor: unknown, msg: string): (params: unknown) => boolean | string {
    return function equal(value: unknown): boolean | string {
      return !_.isEqual(value, equalFor) ? msg : true;
    };
  }
}

export { ValidatorsUtils };
