import { ref } from 'vue';

import { createProvidedComposable } from '.';

const kUseSessionEditor = Symbol('useSessionEditor');

export const { useSessionEditor, useProvidedSessionEditor, provideSessionEditor } =
  createProvidedComposable(kUseSessionEditor, 'sessionEditor', () => {
    let sensors = ref<[]>();
    let nodes = ref<[]>();

    return {};
  });
