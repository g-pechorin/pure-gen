open<@name/> :: <@take/>Effect (SF <@name/>Signal (Maybe <@name/>Event))
open<@name/><@args/> = do
  fsf <- _<@name/>
    -- null
      (\_ -> Nothing)
    -- messages
      <@just/>
    -- structs
      <@news/>
    -- args
      <@args/>
  let s = Lift $ _<@name/>Signal fsf
  let e = Lift $ _<@name/>Event fsf
  pure $ Pipe {take: s, send: e}
