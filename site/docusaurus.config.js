const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

(module.exports = {
  title: 'Replica',
  tagline: 'Android library for organizing of network communication in a declarative way.',
  url: 'https://aartikov.github.io',
  baseUrl: '/Replica/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'aartikov',
  projectName: 'Replica',

  presets: [
    [
      '@docusaurus/preset-classic',
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/aartikov/Replica/tree/main/site',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    ({
      navbar: {
        title: 'Replica',
        logo: {
          alt: 'Replica Logo',
          src: 'img/logo.png',
        },
        items: [
          {
            type: 'doc',
            docId: 'introduction',
            label: 'Documentation',
            position: 'left',
          },
          {
            href: 'pathname:///api/',
            position: 'right',
            label: 'API',
          },
          {
            href: 'https://github.com/aartikov/Replica',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            label: 'Documentation',
            href: '/docs/introduction',
          },
          {
            label: 'API',
            href: 'pathname:///api/',
          },
          {
            label: 'GitHub',
            href: 'https://github.com/aartikov/Replica',
          }
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Artur Artikov. Built with <a href="https://docusaurus.io/">Docusaurus</a>.`,
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
});
