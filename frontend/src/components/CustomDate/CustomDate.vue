<script setup lang="ts">
import type { QDateProps, QInputProps } from 'quasar';
import { computed } from 'vue';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';

type InputProps = Omit<Partial<QInputProps>, 'modelValue'>;
type DateProps = Omit<Partial<QDateProps>, 'modelValue'>;

interface Props extends InputProps {
  modelValue?: Date | Dayjs | undefined;
  dateProps?: DateProps;
  type: 'date' | 'datetime-local' | 'time';
}

const props = defineProps<Props>();
const emit = defineEmits<(e: 'update:modelValue', value: Date | Dayjs | undefined) => void>();

// Computed principal: modelValue <-> string (input)
const dateString = computed({
  get: () => {
    if (!props.modelValue) return '';
    const d = dayjs(props.modelValue);
    if (!d.isValid()) return '';
    return d.format(props.dateProps?.mask ?? 'DD/MM/YYYY');
  },
  set: (val: string) => {
    console.log('dateString', dayjs(val, props.dateProps?.mask ?? 'DD/MM/YYYY'));
    const d = val ? dayjs(val, props.dateProps?.mask ?? 'DD/MM/YYYY') : undefined;
    emit('update:modelValue', d?.isValid() ? d.toDate() : undefined);
  },
});

// Computed auxiliar para o QDate (sempre usa YYYY/MM/DD)
const datePickerString = computed({
  get: () => {
    if (!props.modelValue) return '';
    const d = dayjs(props.modelValue);
    return d.isValid() ? d.format('YYYY/MM/DD') : '';
  },
  set: (val: string) => {
    console.log('datePickerString', dayjs(val, 'YYYY/MM/DD'));
    const d = val ? dayjs(val, 'YYYY/MM/DD') : undefined;
    emit('update:modelValue', d?.isValid() ? d.toDate() : undefined);
  },
});

// Bind das props do input exceto v-model
const inputProps = computed((): Omit<Props, 'modelValue' | 'dateProps'> => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { modelValue, dateProps, mask, ...rest } = props;
  rest.type = rest.type ?? 'date';
  return {
    ...(rest ?? {}),
  } as Omit<Props, 'modelValue' | 'dateProps'>;
});

// Bind das props do q-date exceto v-model
const qDateProps = computed((): DateProps => {
  return {
    mask: props.dateProps?.mask ?? 'DD/MM/YYYY',
    ...(props.dateProps ?? {}),
  };
});
</script>

<template>
  <q-input class="hide-native-date-icon" v-model="dateString" v-bind="inputProps">
    <template v-slot:append>
      <q-icon name="event" class="cursor-pointer">
        <q-popup-proxy cover transition-show="scale" transition-hide="scale">
          <q-date v-model="datePickerString" v-bind="qDateProps">
            <slot />
          </q-date>
        </q-popup-proxy>
      </q-icon>
    </template>
  </q-input>
</template>

<style lang="scss" scoped></style>
