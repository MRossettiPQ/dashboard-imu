import type { InjectionKey} from 'vue';
import { capitalize, inject, provide } from 'vue';

// 1. O tipo agora recebe os Argumentos (Args) e o Retorno (Ret) separadamente
type ProvidedComposable<Args extends unknown[], Ret, N extends string> = {
  [K in `use${Capitalize<N>}`]: (...args: Args) => Ret;
} & {
  [K in `provide${Capitalize<N>}`]: (val: Ret) => void;
} & {
  [K in `useProvided${Capitalize<N>}`]: () => Ret;
};

// 2. A função passa a usar inferência nos argumentos e no retorno
export function createProvidedComposable<
  Args extends unknown[],
  Ret,
  N extends string
>(
  uniqueKey: string | InjectionKey<Ret>,
  name: N,
  composable: (...args: Args) => Ret,
): ProvidedComposable<Args, Ret, N> {
  const capitalizedName = capitalize(name);

  return {
    [`use${capitalizedName}`]: composable,
    [`provide${capitalizedName}`]: (value: Ret) => provide(uniqueKey, value),
    [`useProvided${capitalizedName}`]: () => {
      const value = inject<Ret>(uniqueKey);
      if (!value)
        throw new Error(
          `Failed to use "useProvided${capitalizedName}" because the ${name} was not provided`,
        );
      return value;
    },
  } as ProvidedComposable<Args, Ret, N>;
}
