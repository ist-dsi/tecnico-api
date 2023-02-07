<template>
  <footer class="info-footer">
    <div class="info-footer__container">
      <h1 class="info-footer__title h5">
        Direção de Serviços de Informática (SI)
      </h1>
      <div class="info-footer__informations">
        <ul>
          <li class="is-semibold">
            Campus Alameda
          </li>
          <li>Instituto Superior Técnico</li>
          <li>Av. Rovisco Pais, 1, 1049-001 Lisboa</li>
          <li>Tel: <a href="tel:+351218417506">+351 218 417 506</a> (ext. 1506)</li>
          <li>
            <a
              href="mailto:si@tecnico.ulisboa.pt"
              title="si@tecnico.ulisboa.pt"
            >si@tecnico.ulisboa.pt</a>
          </li>
        </ul>
        <ul>
          <li class="is-semibold">
            Campus Taguspark
          </li>
          <li>Núcleo de Informática do Taguspark (NIT)</li>
          <li>Av. Prof. Doutor Aníbal Cavaco Silva</li>
          <li>2744-016 Porto Salvo</li>
          <li>Tel: <a href="tel:+351214233214">+351 214 233 214</a> (ext. 5014)</li>
        </ul>
      </div>
      <div class="info-footer__default-links">
        <ul class="info-footer__utility-links">
          <li>
            <a href="https://tecnico.ulisboa.pt/en/about-tecnico/">About Técnico</a>
          </li>
          <li>
            <a href="https://tecnico.ulisboa.pt/en/contacts/">General Contacts</a>
          </li>
          <li>
            <a href="https://tecnico.ulisboa.pt/en/informations/terms-conditions/">Terms and Conditions</a>
          </li>
        </ul>
        <p class="info-footer__copyrights">
          1997 – {{ currentYear }} &copy; <a
            href="https://tecnico.ulisboa.pt"
            title="Instituto Superior Técnico"
          >Instituto Superior Técnico</a>,
          <a
            href="https://www.ulisboa.pt"
            title="Universidade de Lisboa"
          >Universidade de Lisboa</a>
        </p>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'Footer',
  data () {
    return {
      currentYear: new Date().getFullYear()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~/styles/_variables.scss';

.info-footer {
  padding: 3.75rem 0.9375rem 0;
  background-color: $dark;
  color: white;
  font-size: 0.875rem;

  a {
    transition: unset;
    &:hover {
      color: $blue;
      border-bottom: unset;
    }
  }
}

.info-footer__title {
  margin-bottom: 1.5rem;
  color: white;
}

.info-footer__informations {
  display: flex;
  flex-flow: row wrap;
  margin-bottom: 2rem;

  & > * {
    margin-right: 6rem;

    &:last-child {
      margin-right: 0;
    }
  }
}

.info-footer__default-links {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-flow: row wrap;
}

.info-footer__utility-links {
  list-style-type: none;
  min-width: 100%;
  padding: 2rem 0;
  display: flex;
  flex-flow: column nowrap;
  margin-left: -0.5rem;

  @media screen and (min-width: 47.5rem) {
    min-width: 430px;
    flex-flow: row nowrap;
    justify-content: flex-start;
    align-items: flex-start;
  }

  a {
    display: block;
    padding: 0.5rem 0;
    margin: 0 0.5rem;

    &:link,
    &:visited {
      color: rgba(255, 255, 255, 0.25);
    }
  }
}

.info-footer__copyrights {
  margin: 0;
  padding: 0;
  min-width: 100%;
  color: rgba(255, 255, 255, 0.25);
  font-size: inherit;

  @media screen and (min-width: 64rem) {
    margin-left: auto;
    padding: 2rem 0;
    min-width: 430px;
    align-self: center;
    text-align: right;
  }

  a {
    white-space: nowrap;
    color: rgba(255, 255, 255, 0.25);
    transition: border-bottom 0.2s ease;
    border-bottom: 1px dotted transparent;

    &:hover,
    &:focus {
      color: unset;
      border-bottom: 1px dotted rgba(255, 255, 255, 0.25);
    }
  }
}
</style>
