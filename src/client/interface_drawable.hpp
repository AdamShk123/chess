#ifndef CHESS_INTERFACE_DRAWABLE_HPP
#define CHESS_INTERFACE_DRAWABLE_HPP

#include <SDL3/SDL_render.h>

namespace Game
{
    struct TextureDestroyer
    {
        void operator()(SDL_Texture* texture)
        {
            SDL_DestroyTexture(texture);
        }
    };

    using Texture = std::unique_ptr<SDL_Texture,TextureDestroyer>;

    class Drawable
    {
    public:
        Drawable(int x, int y, const Texture& texture, SDL_Renderer* renderer) : m_x(x), m_y(y), m_texture(texture), m_renderer(renderer)
        {
            float w,h;
            SDL_GetTextureSize(texture.get(), &w, &h);
            m_w = static_cast<int>(w);
            m_h = static_cast<int>(h);
        }
        virtual ~Drawable() = default;
        virtual auto draw() -> void = 0;
    protected:
        int m_x;
        int m_y;
        int m_w;
        int m_h;

        const Texture& m_texture;

        SDL_Renderer* m_renderer;
    };
}

#endif //CHESS_INTERFACE_DRAWABLE_HPP
