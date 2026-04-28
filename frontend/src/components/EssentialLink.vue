<template>
  <q-item
    clickable
    v-bind="$attrs"
    v-if="to"
    :to="to"
    class="nav-link"
    active-class="nav-link--active"
  >
    <q-item-section v-if="icon" avatar>
      <div class="nav-link__icon-wrap">
        <q-icon :name="icon" />
      </div>
    </q-item-section>

    <q-item-section>
      <q-item-label class="nav-link__title">{{ title }}</q-item-label>
      <q-item-label caption class="nav-link__caption">{{ caption }}</q-item-label>
    </q-item-section>
  </q-item>

  <q-item clickable v-bind="$attrs" v-else-if="link" :href="link" target="_blank" class="nav-link">
    <q-item-section v-if="icon" avatar>
      <div class="nav-link__icon-wrap">
        <q-icon :name="icon" />
      </div>
    </q-item-section>

    <q-item-section>
      <q-item-label class="nav-link__title">{{ title }}</q-item-label>
      <q-item-label caption class="nav-link__caption">{{ caption }}</q-item-label>
    </q-item-section>
  </q-item>

  <q-item clickable v-bind="$attrs" v-else class="nav-link">
    <q-item-section v-if="icon" avatar>
      <div class="nav-link__icon-wrap">
        <q-icon :name="icon" />
      </div>
    </q-item-section>

    <q-item-section>
      <q-item-label class="nav-link__title">{{ title }}</q-item-label>
      <q-item-label caption class="nav-link__caption">{{ caption }}</q-item-label>
    </q-item-section>
  </q-item>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export interface EssentialLinkProps {
  title: string;
  caption?: string;
  link?: string; // external URL
  icon?: string;
  to?: { name: string }; // Vue Router route object
  authenticated?: boolean;
  hiddenAuthenticated?: boolean;
}

export default defineComponent({
  name: 'EssentialLink',
  props: {
    title: {
      type: String,
      required: true,
    },
    caption: {
      type: String,
      default: '',
    },
    link: {
      type: String,
      default: '',
    },
    icon: {
      type: String,
      default: '',
    },
    to: {
      type: Object as () => { name: string },
      default: null,
    },
  },
});
</script>

<style lang="scss" scoped>
.nav-link {
  margin: 4px 10px;
  border-radius: 12px;
  padding: 10px 14px;
  min-height: 48px;
  transition:
    background 0.2s ease,
    transform 0.15s ease;

  &:hover {
    background: rgba($primary, 0.06);
    transform: translateX(2px);
  }

  &--active {
    background: rgba($primary, 0.1);

    .nav-link__icon-wrap {
      background: $primary;
      color: #ffffff;
    }

    .nav-link__title {
      color: $primary;
      font-weight: 700;
    }
  }

  &__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 36px;
    height: 36px;
    border-radius: 10px;
    background: rgba($primary, 0.08);
    color: $primary;
    font-size: 20px;
    transition:
      background 0.2s ease,
      color 0.2s ease;
  }

  &__title {
    font-size: 14px;
    font-weight: 600;
    color: #1a1a2e;
    letter-spacing: 0.1px;
  }

  &__caption {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 1px;
  }
}
</style>
